/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.utils;

/**
 *
 * @author israa-edafa
 */
public class StringUtils {

    public static String logTrxId(String trxId) {
        StringBuilder sb = new StringBuilder();
        sb.append("UserTrx");
        sb.append("(");
        sb.append(trxId);
        sb.append("): ");
        return sb.toString();
    }

    public static String userLogInfo(String id, String userName) {
        StringBuilder sb = new StringBuilder();
        sb.append("User");
        sb.append("(");
        sb.append(id);
        sb.append(",");
        sb.append(userName);
        sb.append("): ");
        return sb.toString();
    }

    public static boolean stringExists(String input) {
        return (input != null && !input.isEmpty());
    }

    public static String concatenate(Object... input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            sb.append(input[i]);
        }
        return sb.toString();
    }
}
