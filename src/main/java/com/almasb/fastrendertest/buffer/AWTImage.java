package com.almasb.fastrendertest.buffer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

import javafx.scene.image.*;
import javafx.util.Callback;

/** 
 * A simple wrapper arround an AWT image which utilizes the new WritableImage
 * of JavaFX 13 with support for Buffers. Internally a JavaFX image is created
 * which directly uses the same memory as the AWT image. So if you render
 * into the AWT image with a AWT graphics context, the result will immediately
 * appear on the screen.
 * 
 * @author Michael Paus
 */
public class AWTImage {
	
	private BufferedImage awtImage;
	private Graphics2D g2d;
	private PixelBuffer<IntBuffer> pixelBuffer;
	private WritableImage fxImage;

	private ImageView view;

	/**
	 * Constructs an internal BufferedImage with the given width and height.
	 * 
	 * @param width image width
	 * @param height image height
	 */
	public AWTImage(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE));
	}
	
	/**
	 * Wraps an already existing BufferedImage. (Must be of type TYPE_INT_ARGB_PRE).
	 * 
	 * @param awtImage the image to be wrapped.
	 */
	public AWTImage(BufferedImage awtImage) {
		this.awtImage = awtImage;
		g2d = (Graphics2D) awtImage.getGraphics();

		DataBuffer db = awtImage.getRaster().getDataBuffer();
		DataBufferInt dbi = (DataBufferInt) db;
		int[] rawInts = dbi.getData();
		IntBuffer ib = IntBuffer.wrap(rawInts);

		PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
		pixelBuffer = new PixelBuffer<>(awtImage.getWidth(), awtImage.getHeight(), ib, pixelFormat);
		fxImage = new WritableImage(pixelBuffer);
		view = new ImageView(fxImage);
	}

	public ImageView getView() {
		return view;
	}

	public Graphics2D getGraphicsContext() {
		return g2d;
	}

	/**
	 * Get access to the internal JavaFX image.
	 * 
	 * @return the internal JavaFX image.
	 */
	public Image getFXImage() {return fxImage;}
	
	/**
     * Get access to the internal AWT image.
     * 
	 * @return the internal AWT image.
	 */
	public BufferedImage getAWTImage() {return awtImage;}
	
	/**
	 * Get the width of the image.
	 * 
	 * @return the width of the image.
	 */
	public int getWidth() {return awtImage.getWidth();}
	
	/**
	 * Get the height of the image.
	 * 
	 * @return the height of the image.
	 */
	public int getHeight() {return awtImage.getHeight();}

	public void render() {
		pixelBuffer.updateBuffer(r -> null);
	}
}