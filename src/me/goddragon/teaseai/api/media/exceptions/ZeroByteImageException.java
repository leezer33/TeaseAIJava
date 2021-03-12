/**
 * Created by leezer3
 */
 
package me.goddragon.teaseai.api.Exceptions;
 
public class ZeroByteImageException extends Exception {

    public ZeroByteImageException(String message) {
        super(message);
    }
	
	 public ZeroByteImageException() {
        super();
    }
}