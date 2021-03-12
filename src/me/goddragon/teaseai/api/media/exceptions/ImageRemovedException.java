/**
 * Created by leezer3
 */
 
package me.goddragon.teaseai.api.Exceptions;

public class ImageRemovedException extends Exception {

    public ImageRemovedException(String message) {
        super(message);
    }
	
	public ImageRemovedException() {
        super();
    }
}