package com;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;

public class MyBank extends Applet {

	/* Constants */
	public static final byte CLA_MONAPPLET = (byte) 0xB0;
	public static final byte INS_TEST_CODE_PIN = 0x00;
	public static final byte INS_INTERROGER_COMPTE = 0x01;
	public static final byte INS_INCREMENTER_COMPTE = 0x02;
	public static final byte INS_DECREMENTER_COMPTE = 0x03;
	public static final byte INS_INITIALISER_COMPTE = 0x04;

	public final static short MAX_BALANCE = 0x01F4;// le maximum de la balance

	public final static byte MAX_MONTANT_TRANSACTION = (byte)127;// maximum montant
															// qu'on peut
															// transiter

	public final static byte MAX_ERROR_PIN = (byte) 0x03;// maximum de code pin
															// erroner

	public final static byte MAX_PIN_LENGTH = (byte) 0x04;// longeur maximale du
															// code pin

	
	private byte[] INIT_PIN = { (byte) 2, (byte) 0,(byte) 0,(byte) 1 };
	
	/* Exception */

	// Verification Pin Echoué
	final static short SW_VERIFICATION_FAILED = 0x6300;

	final static short SW_EXCEED_TRY_LIMIT = 0x6321;
	// signal the the PIN validation is required
	// for a credit or a debit transaction

	// signal that the balance exceed the maximum
	final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
	// signal the the balance becomes negative
	final static short SW_NEGATIVE_BALANCE = 0x6A85;


	/* variables */
	OwnerPIN pin;
	static short balance ;
	

	private MyBank(byte[] bArray, short i, short j) {
	    pin = new OwnerPIN(MAX_ERROR_PIN, MAX_PIN_LENGTH);
	    pin.update(INIT_PIN, (short) 0, (byte) 0x04);
	}


	public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
		new MyBank(bArray,bOffset,bLength).register();
	}
	
	public boolean select() {

		// pas de selection si le pin est blocker
		pin.resetAndUnblock();
		return true;
	}
	
	public void deselect() {
	    pin.reset();
	}

	public void process(APDU apdu) {
		// Buffer=Objet APDU porte un tableau tampon de byte qui transfére
				// l'entete + data entre la carte et le CAD
				// du APDU entrant et sortant

				byte[] buffer = apdu.getBuffer();

				// exception qui teste sur la commande de selection
				if (apdu.isISOInterindustryCLA()) {
					if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
						return;
					} else {
						ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
					}
				}

				// Verifier si reinitialisation a une CLA correcte qui spécifie la
				// structure de commandement
				if (this.selectingApplet())
					return;
				if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
					ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
				}

				switch (buffer[ISO7816.OFFSET_INS]) {
				case INS_TEST_CODE_PIN:
					verify(apdu);
					break;
				case INS_INCREMENTER_COMPTE:
					credit(apdu);
					break;
				case INS_DECREMENTER_COMPTE:
					debit(apdu);
					break;
				case INS_INTERROGER_COMPTE:
					getBalance(apdu);
					break;

				default:
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
				}

			}
	
	
	private void credit(APDU apdu) {
		

		byte[] buffer = apdu.getBuffer();

		// Lc byte denotes the number of bytes in the
		// data field of the command APDU
		byte numBytes = buffer[ISO7816.OFFSET_LC];

		// indicate that this APDU has incoming data
		// and receive data starting from the offset
		// ISO7816.OFFSET_CDATA following the 5 header
		// bytes.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// it is an error if the number of data bytes
		// read does not match the number in Lc byte
		if ((numBytes != 1) || (byteRead != 1))
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		// get the credit amount
		byte creditAmount = buffer[ISO7816.OFFSET_CDATA];

		// check the new balance
		if ((short) (balance + creditAmount) > MAX_BALANCE)
			ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);

		// credit the amount
		balance = (short) (balance + creditAmount);

		
	}
	
	private void debit(APDU apdu) {


		byte[] buffer = apdu.getBuffer();

		byte numBytes = (byte) (buffer[ISO7816.OFFSET_LC]);

		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		if ((numBytes != 1) || (byteRead != 1))
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		// get debit amount
		byte debitAmount = buffer[ISO7816.OFFSET_CDATA];

		// check the new balance
		if ((short) (balance - debitAmount) < (short) 0)
			ISOException.throwIt(SW_NEGATIVE_BALANCE);

		balance = (short) (balance - debitAmount);

		
	}
	
	
	private void getBalance(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		
		short le = apdu.setOutgoing();

		if (le < 2)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		apdu.setOutgoingLength((byte) 2);
		
		buffer[0] = (byte) (balance >> 8);
		buffer[1] = (byte) (balance & 0xFF);
		
		Util.setShort(buffer, (short)0, balance);
		
		apdu.sendBytes((short) 0, (short) 2);
		
	}
	
	private void verify(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// retrieve the PIN data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());
		// check pin
		// the PIN data is read into the APDU buffer
		// at the offset ISO7816.OFFSET_CDATA
		// the PIN data length = byteRead
		if(pin.getTriesRemaining()==(byte) 0)
			ISOException.throwIt(SW_EXCEED_TRY_LIMIT);
		if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false)
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		
	}
	


}

