/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.exception;

/**
 *
 * @author libertyerp_local
 */
public class ForeignKeyConstraintException extends RuntimeException {

    public ForeignKeyConstraintException(String message, Throwable cause) {
        super(message);
    }
}
