package com.edafa.web2sms.utils.sms;

import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.sms.exception.InvalidAddressFormattingException;
import com.edafa.web2sms.utils.sms.exception.InvalidMSISDNFormatException;
import com.edafa.web2sms.utils.sms.exception.InvalidSMSReceiver;
import com.edafa.web2sms.utils.sms.exception.InvalidSMSSender;


public class SMSUtils {
	static final long serialVersionUID = 6779748747310201588L;

	public static final int engMsgMaxLen = 160;
	public static final int nonEngMsgMaxLen = 70;
	public static final int engConcMsgLen = 153;
	public static final int nonEngConcMsgLen = 67;

	// MSISDN InnerClass

	public static class MSISDN {
		public static String getMSISDN_INTER_KEY() {
			return "00";
		}

		public static String getMSISDN_INTER_PLUS() {
			return "+";
		}

		public static String getMSISDN_NATIONAL_KEY() {
			return (String) Configs.MSISDN_NATIONAL_KEY.getValue();
		}

		public static String getMSISDN_CC() {
			return (String) Configs.MSISDN_CC.getValue();
		}

		public static String getMSISDN_INTER_KEY_CC() {
			return getMSISDN_INTER_KEY() + getMSISDN_CC();
		}

		public static String getMSISDN_INTER_PLUS_CC() {
			return getMSISDN_INTER_PLUS() + getMSISDN_CC();
		}

		public static int getMSISDN_INTER_KEY_CC_LEN() {
			return getMSISDN_INTER_KEY_CC().length() + (Integer) Configs.MSISDN_NDC_LEN.getValue()
					+ (Integer) Configs.MSISDN_SN_LEN.getValue();
		}

		public static int getMSISDN_INTER_PLUS_CC_LEN() {
			return (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue()
					+ getMSISDN_INTER_PLUS_CC().length();
		}

		public static int getMSISDN_CC_LEN() {
			return (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue()
					+ getMSISDN_CC().length();
		}

		public static int getMSISDN_NATIONAL_KEY_LEN() {
			return (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue()
					+ getMSISDN_NATIONAL_KEY().length();
		}

		public static int getMSISDN_NATIONAL_LEN() {
			return (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue();
		}

	}

	// Validations
	public static SenderType getSenderType(String sender) throws InvalidSMSSender {
		if (sender == null || sender.isEmpty())
			throw new InvalidSMSSender("", "Null or empty sender");
		int alphaNumSenderLen = (Integer) Configs.ALPHANUM_SENDER_LENGTH.getValue();
		int shortCodeSenderLen = (Integer) Configs.SHORT_CODE_SENDER_LENGTH.getValue();
		if (isAlphnum(sender)) {
			if (sender.length() <= alphaNumSenderLen) {
				return SenderType.ALPHANUMERIC;
			} else {
				throw new InvalidSMSSender(sender, "Invalid alphanumeric sender length");
			}
		} else if (sender.length() <= shortCodeSenderLen && sender.matches("[0-9]+")) {
			return SenderType.SHORT_CODE;
		} else if (validateNationalAddress(sender)) {
			return SenderType.NATIONAL;
		} else if (validateInternationalAddress(sender)) {
			return SenderType.INTERNATIONAL;
		} else {
			throw new InvalidSMSSender(sender, "Invalid sender length");
		}
	}

	public static ReceiverType getReceiverType(String receiver) throws InvalidSMSReceiver {
		if (validateNationalAddress(receiver)) {
			return ReceiverType.NATIONAL;
		} else if (validateInternationalAddress(receiver)) {
			return ReceiverType.INTERNATIONAL;
		} else {
			throw new InvalidSMSReceiver(receiver, "Invalid SMS receiver");
		}
	}

	boolean isValid() {
		return true;
	}

	// public static
	public static int calcSegCount(LanguageNameEnum lang, String smsText) {
		int totalSegments = 1;
		int msgMaxLen = 0;
		int msgLength = smsText.length();

		switch (lang) {
		case ENGLISH:
			int extCharCount = 0;
			char[] smsChars = smsText.toCharArray();
			for (char c : smsChars) {
				switch (c) {
				case '^':
				case '\u000c':
				case '{':
				case '}':
				case '\\':
				case '[':
				case ']':
				case '~':
				case '|':
				case '\u20AC':
					extCharCount++;
					break;
				default:
					break;
				}
			}

			// Since GSM7Bit extended character set takes two bytes
			msgLength += extCharCount;

			msgMaxLen = engMsgMaxLen;
			if (msgLength > msgMaxLen) {
				msgMaxLen = engConcMsgLen;
				totalSegments = (int) Math.ceil((double) msgLength / msgMaxLen);

			}
			break;

		default:
			msgMaxLen = nonEngMsgMaxLen;
			if (msgLength > msgMaxLen) {
				msgMaxLen = nonEngConcMsgLen;
				totalSegments = (int) Math.ceil((double) msgLength / msgMaxLen);
			}
		}

		return totalSegments;
	}

	protected static boolean isAlphnum(String sender) {
		return (sender.matches("[\\p{Alnum}\\p{Punct} &&[^><:]]+"));
	}

	public static String formatAddress(String addr, MsisdnFormat resultFormat) throws InvalidMSISDNFormatException,
			InvalidAddressFormattingException {

		MsisdnFormat srcAddrFormat = getAddressFormat(addr);
		return formatAddress(addr, srcAddrFormat, resultFormat);
	}

	public static String formatAddress(String addr, MsisdnFormat srcAddrFormat, MsisdnFormat resultFormat)
			throws InvalidAddressFormattingException {

		if (srcAddrFormat.equals(resultFormat))
			return addr;

		switch (srcAddrFormat) {
		case INTER_KEY_CC_LOCAL:
			switch (resultFormat) {
			case INTER_KEY:
			case INTER_KEY_CC_LOCAL:
				break;
			case INTER_PLUS:
			case INTER_PLUS_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY_CC(), MSISDN.getMSISDN_INTER_PLUS_CC());
				break;
			case INTER:
			case INTER_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY_CC(), MSISDN.getMSISDN_CC());
				break;
			case NATIONAL_KEY:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY_CC(), MSISDN.getMSISDN_NATIONAL_KEY());
				break;
			case NATIONAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY_CC(), "");
				break;
			}
			break;
		case INTER_PLUS_CC_LOCAL:
			switch (resultFormat) {
			case INTER_KEY:
			case INTER_KEY_CC_LOCAL:
				addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS_CC(), MSISDN.getMSISDN_INTER_KEY_CC());
				break;
			case INTER_PLUS:
			case INTER_PLUS_CC_LOCAL:
				break;
			case INTER:
			case INTER_CC_LOCAL:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS_CC(), MSISDN.getMSISDN_CC());
				break;
			case NATIONAL_KEY:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS_CC(), MSISDN.getMSISDN_NATIONAL_KEY());
				break;
			case NATIONAL:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS_CC(), "");
				break;
			}
			break;
		case INTER_CC_LOCAL:
			switch (resultFormat) {
			case INTER_KEY:
			case INTER_KEY_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_CC(), MSISDN.getMSISDN_INTER_KEY_CC());
				break;
			case INTER_PLUS:
			case INTER_PLUS_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_CC(), MSISDN.getMSISDN_INTER_PLUS_CC());
				break;
			case INTER:
			case INTER_CC_LOCAL:
				break;
			case NATIONAL_KEY:
				addr = addr.replaceFirst(MSISDN.getMSISDN_CC(), MSISDN.getMSISDN_NATIONAL_KEY());
				break;
			case NATIONAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_CC(), "");
				break;
			}
			break;
		case NATIONAL_KEY:
			switch (resultFormat) {
			case INTER_KEY:
			case INTER_KEY_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_NATIONAL_KEY(), MSISDN.getMSISDN_INTER_KEY_CC());
				break;
			case INTER_PLUS:
			case INTER_PLUS_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_NATIONAL_KEY(), MSISDN.getMSISDN_INTER_PLUS_CC());
				break;
			case INTER:
			case INTER_CC_LOCAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_NATIONAL_KEY(), MSISDN.getMSISDN_CC());
				break;
			case NATIONAL_KEY:
				break;
			case NATIONAL:
				addr = addr.replaceFirst(MSISDN.getMSISDN_NATIONAL_KEY(), "");
				break;
			}
			break;
		case NATIONAL:
			switch (resultFormat) {
			case INTER_KEY:
			case INTER_KEY_CC_LOCAL:
				addr = MSISDN.getMSISDN_INTER_KEY_CC() + addr;
				break;
			case INTER_PLUS:
			case INTER_PLUS_CC_LOCAL:
				addr = MSISDN.getMSISDN_INTER_PLUS_CC() + addr;
				break;
			case INTER:
			case INTER_CC_LOCAL:
				addr = MSISDN.getMSISDN_CC() + addr;
				break;
			case NATIONAL_KEY:
				addr = MSISDN.getMSISDN_NATIONAL_KEY() + addr;
				break;
			case NATIONAL:
				break;
			}
			break;
		case INTER_KEY:
			switch (resultFormat) {
			case INTER_KEY:
				break;
			case INTER_PLUS:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY(), MSISDN.getMSISDN_INTER_PLUS());
				break;
			case INTER:
				addr = addr.replaceFirst(MSISDN.getMSISDN_INTER_KEY(), "");
				break;
			case INTER_CC_LOCAL:
			case INTER_KEY_CC_LOCAL:
			case INTER_PLUS_CC_LOCAL:
			case NATIONAL:
			case NATIONAL_KEY:
			default:
				throw new InvalidAddressFormattingException(srcAddrFormat, resultFormat);
			}
			break;
		case INTER_PLUS:
			switch (resultFormat) {
			case INTER_PLUS:
				break;
			case INTER_KEY:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS(), MSISDN.getMSISDN_INTER_KEY());
				break;
			case INTER:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS(), "");
				break;
			case INTER_CC_LOCAL:
			case INTER_KEY_CC_LOCAL:
			case INTER_PLUS_CC_LOCAL:
			case NATIONAL_KEY:
			case NATIONAL:
				throw new InvalidAddressFormattingException(srcAddrFormat, resultFormat);
			}
			break;
		case INTER:
			switch (resultFormat) {
			case INTER_PLUS:
				break;
			case INTER_KEY:
				addr = addr.replaceFirst("\\" + MSISDN.getMSISDN_INTER_PLUS(), MSISDN.getMSISDN_INTER_KEY());
				break;
			case INTER:
				break;
			case INTER_CC_LOCAL:
			case INTER_KEY_CC_LOCAL:
			case INTER_PLUS_CC_LOCAL:
			case NATIONAL_KEY:
			case NATIONAL:
				throw new InvalidAddressFormattingException(srcAddrFormat, resultFormat);
			}
		}

		return addr;
	}

	protected static MsisdnFormat getAddressFormat0(String address) {

		int msisdnSNLen = (Integer) Configs.MSISDN_SN_LEN.getValue();
		String ndcRegex = (String) Configs.NDC_REGEX.getValue();
		int msisdnLen = address.length();
		MsisdnFormat result = null;

		if (address.startsWith(MSISDN.getMSISDN_INTER_KEY_CC())) {
			if (msisdnLen == MSISDN.getMSISDN_INTER_KEY_CC_LEN()
					&& address.matches(MSISDN.getMSISDN_INTER_KEY_CC() + ndcRegex + "[0-9]{" + msisdnSNLen + "}")) {
				result = MsisdnFormat.INTER_KEY_CC_LOCAL;
			}
		} else if (address.startsWith(MSISDN.getMSISDN_INTER_PLUS_CC())) {
			if (msisdnLen == MSISDN.getMSISDN_INTER_PLUS_CC_LEN()
					&& address.matches("\\" + MSISDN.getMSISDN_INTER_PLUS_CC() + ndcRegex + "[0-9]{" + msisdnSNLen
							+ "}")) {
				result = MsisdnFormat.INTER_PLUS_CC_LOCAL;
			}

		} else if (address.startsWith(MSISDN.getMSISDN_CC())) {
			if (msisdnLen == MSISDN.getMSISDN_CC_LEN()
					&& address.matches(MSISDN.getMSISDN_CC() + ndcRegex + "[0-9]{" + msisdnSNLen + "}")) {
				result = MsisdnFormat.INTER_CC_LOCAL;
			}
		} else if (msisdnLen == MSISDN.getMSISDN_NATIONAL_KEY_LEN()
				&& address.matches(MSISDN.getMSISDN_NATIONAL_KEY() + ndcRegex + "[0-9]{" + msisdnSNLen + "}")) {
			result = MsisdnFormat.NATIONAL_KEY;
		} else if (msisdnLen == MSISDN.getMSISDN_NATIONAL_LEN()
				&& address.matches(ndcRegex + "[0-9]{" + msisdnSNLen + "}")) {
			result = MsisdnFormat.NATIONAL;
		} else if (address.matches(MSISDN.getMSISDN_INTER_KEY() + "[1-9][0-9]{5,14}")) {
			result = MsisdnFormat.INTER_KEY;
		} else if (address.matches("\\" + MSISDN.getMSISDN_INTER_PLUS() + "[1-9][0-9]{5,14}")) {
			result = MsisdnFormat.INTER_PLUS;
		} 
//		else if (address.matches("[1-9][0-9]{5,14}")) {
//			result = MsisdnFormat.INTER;
//		}

		return result;
	}

	public static MsisdnFormat getAddressFormat(String address) throws InvalidMSISDNFormatException {

		MsisdnFormat result = getAddressFormat0(address);
		if (result == null)
			throw new InvalidMSISDNFormatException();
		return result;
	}

	public static boolean validateNationalAddress(String address) {
		try {
			MsisdnFormat addrFormat = getAddressFormat0(address);
			switch (addrFormat) {
			case NATIONAL_KEY:
			case NATIONAL:
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean validateLocalAddress(String address) {

		try {
			MsisdnFormat addrFormat = getAddressFormat0(address);
			switch (addrFormat) {
			case NATIONAL:
			case NATIONAL_KEY:
			case INTER_CC_LOCAL:
			case INTER_KEY_CC_LOCAL:
			case INTER_PLUS_CC_LOCAL:
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean validateInternationalAddress(String address) {
		try {
			MsisdnFormat addrFormat = getAddressFormat0(address);
			switch (addrFormat) {
			case INTER:
			case INTER_KEY:
			case INTER_PLUS:
			case INTER_CC_LOCAL:
			case INTER_KEY_CC_LOCAL:
			case INTER_PLUS_CC_LOCAL:
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

	public static boolean validateLanguage(String lang) {
		try {
			// try to get its equivalent lang enum
			LanguageNameEnum.valueOf(lang.toUpperCase());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static ReceiverType getReceiverType(MsisdnFormat format) {
		switch (format) {
		case NATIONAL:
		case NATIONAL_KEY:
			return ReceiverType.NATIONAL;
		default:
			return ReceiverType.INTERNATIONAL;
		}
	}

	public static void main(String[] arg) throws InvalidSMSSender, InvalidMSISDNFormatException, InvalidSMSReceiver {
		System.out.println(getAddressFormat0("20102416001"));
		System.out.println(validateInternationalAddress("201024160016"));
		// long time = System.currentTimeMillis();
		// int count = 1000 * 1000;
		// for (int i = 0; i < count; i++) {
		//
		// getAddressFormat0("01024160016");
		//
		// }
		//
		// long resultTime = System.currentTimeMillis() - time;
		// System.out.println(resultTime);
		// System.out.println(((double) resultTime / count));

		// System.out.println(getReceiverType("0020102416001"));
		// getSenderType("test ");
		// String complete;
		// Scanner input;
		// do {
		// input = new Scanner(System.in);
		// System.out.println("plz enter MSISDN: ");
		// String target = input.nextLine();
		// System.out
		// .println("plz choose no. of desired format: \n 1. 002010*** \n 2. +2010*** \n 3. 2010*** \n 4. 010*** \n 5. 10***");
		// int choose = input.nextInt();
		// String result = "";
		// try {
		// result = "";
		// switch (choose) {
		// case 1:
		// result = formatAddress(target, MsisdnFormat.INTER_KEY_CC_LOCAL);
		// break;
		// case 2:
		// result = formatAddress(target, MsisdnFormat.INTER_PLUS_CC_LOCAL);
		// break;
		// case 3:
		// result = formatAddress(target, MsisdnFormat.INTER_CC_LOCAL);
		// break;
		// case 4:
		// result = formatAddress(target, MsisdnFormat.NATIONAL_KEY);
		// break;
		// case 5:
		// result = formatAddress(target, MsisdnFormat.NATIONAL);
		// break;
		// case 6:
		// result = formatAddress(target, MsisdnFormat.INTER);
		// // System.out.println(getAddressFormat(target));
		// // System.out.println(getReceiverType(target));
		//
		// break;
		// default:
		// System.out.println("error no. out of range 1-5 ... ");
		// }
		// } catch (InvalidMSISDNFormatException e) {
		// System.out.println("InvalidMSISDNFormatException");
		// } catch (InvalidAddressFormattingException e) {
		// System.out.println("InvalidAddressFormattingException");
		// }
		// // catch (InvalidSMSReceiver e) {
		// // System.out.println("InvalidSMSReceiver " +
		// // e.getLocalizedMessage());
		// // }
		// System.out.println("the result is: " + result);
		//
		// System.out.println("Do you want to try another test Case (y|n):");
		// complete = input.next();
		//
		// } while (complete.equals("y"));
		// input.close();
		//
	}
}
