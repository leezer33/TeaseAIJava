/**
 * Created by leezer3
 */
 
package me.goddragon.teaseai.api.Exceptions; 

public class UnsupportedImageTypeException extends Exception {

    public UnsupportedImageTypeException(String message) {
        super(message);
    }
	
	public UnsupportedImageTypeException() {
        super();
    }
}