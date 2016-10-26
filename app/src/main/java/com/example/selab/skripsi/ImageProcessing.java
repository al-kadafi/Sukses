/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.selab.skripsi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * 
 * @author k-dafi
 */
public class ImageProcessing {

	Bitmap in = null;
	double max = 0;
	double min = 999;
	int lenghtLineX, lenghtLineY;
	boolean status[][];
	int label[][];

	public ImageProcessing(Bitmap in) {
		this.in = in;
	}

	public void setImage(Bitmap in) {
		this.in = in;
	}

	public void createLine() {
		lenghtLineX = in.getWidth() / 3;
		lenghtLineY = (in.getHeight() / 3);

		// set colour pixel
		for (int i = 0; i < 3; i++) {
			in = createLineX(lenghtLineX, lenghtLineY - i, lenghtLineX);
			in = createLineY(lenghtLineX - i, lenghtLineY - 3);
			in = createLineY((2 * lenghtLineX) - i, lenghtLineY - 3);
		}

	}

	public Bitmap createLineX(int x, int y, int lenght) {
		for (int i = x; i < (x + lenght); i++) {
			in.setPixel(i, y, 0b11111111000000001111111100000000);// set piksel dengan warna hijau
			i++;
		}

		return in;
	}

	public Bitmap createLineY(int x, int y) {
		for (int i = y; i < (in.getHeight()); i++) {
			in.setPixel(x, i, 0b11111111000000001111111100000000); // set piksel dengan warna hijau
			i++;
		}

		return in;
	}

	public int[][] getDataPixel() {

		int data[][] = new int[lenghtLineX - 3][(in.getHeight() - lenghtLineY) - 1];
		status = new boolean[lenghtLineX - 3][(in.getHeight() - lenghtLineY) - 1];
		label = new int[lenghtLineX - 3][(in.getHeight() - lenghtLineY) - 1];

		int startPixelX = lenghtLineX + 1;

		for (int i = 0; i < data.length; i++) {
			int startPixelY = lenghtLineY + 1;

			for (int j = 0; j < data[0].length; j++) {
				int piksel = in.getPixel(startPixelX, startPixelY);

				int A = Color.alpha(piksel);
				int R = Color.red(piksel);
				int G = Color.green(piksel);
				int B = Color.blue(piksel);

				int gray = (int) (R + G + B) / 3;
				in.setPixel(startPixelX, startPixelY,
						Color.argb(A, gray, gray, gray));

				// cek nilai max dan min piksel
				if (gray >= max) {
					max = gray;
				}
				if (gray <= min) {
					min = gray;
				}

				// set nilai dan status array
				data[i][j] = gray;
				status[i][j] = false;
				label[i][j] = 0;

				startPixelY++;
			}
			startPixelX++;
		}
//		Log.d("max", max + "");
//		Log.d("min", min + "");
		return data;
	}

	public Bitmap getImage() {
		return in;
	}

	public int blobProcessing(int data[][]) {
		double threshold = Math.floor(((double) (max + min) / 2)*0.9);
//		Log.d("threshold", threshold + "");
//		Log.d("piksel", in.getPixel(lenghtLineX + 10, lenghtLineY + 10) + "");

		int startPixelX = lenghtLineX + 1;
		int c = 0;
		for (int i = 0; i < data.length; i++) {
			int startPixelY = lenghtLineY + 1;

			for (int j = 0; j < data[0].length; j++) {
				if (label[i][j] == 0) {
					if (data[i][j] <= threshold) {
						c++;
						in.setPixel(startPixelX, startPixelY, 0);

						detectBlob(i, j, threshold, data);
					} else {

						in.setPixel(startPixelX, startPixelY, -1);

					}
				}

				startPixelY++;
			}
			startPixelX++;
		}
//		Log.d("jumlah blob", newLabel + "");
//		Log.d("jumlah blob dari array", label[label.length - 1] + "");

		return filterBlob();

	}

	int rgb = 0b11111111111111010111001100110100;
	int newLabel = 1;
	int totalPixel = 0;
	ArrayList<Integer> jumPixelBlob = new ArrayList<Integer>();

	public void detectBlob(int x, int y, double threshold, int data[][]) {
		Queue<int[]> queue = new LinkedList<int[]>();
		int center[] = { x, y };
		queue.add(center);
		in.setPixel(lenghtLineX + 1 + x, lenghtLineY + 1 + y, rgb);
		label[x][y] = newLabel;
		int jumlahPixel = 0;
		
		while (!queue.isEmpty()) {
			int neighbour[] = queue.remove();
			for (int i = neighbour[0] - 1; i < neighbour[0] + 2; i++) {
				for (int j = neighbour[1] - 1; j < neighbour[1] + 2; j++) {
					if (i >= 0 && i < data.length && j >= 0
							&& j < data[0].length && label[i][j] == 0
							&& data[i][j] <= threshold) {
						label[i][j] = newLabel;

						in.setPixel(lenghtLineX + 1 + i, lenghtLineY + 1 + j,rgb);
						int newNeigbour[] = { i, j };
						queue.add(newNeigbour);

						jumlahPixel++;
					}
				}
			}
		}
		totalPixel += jumlahPixel;
		jumPixelBlob.add(jumlahPixel);
		newLabel++;
		rgb -= 15056;
	}

	public int filterBlob() {
		int batas = totalPixel / (newLabel);
//		Log.d("batas", batas + "");
//		Log.d("jumlah pixel", jumPixelBlob.get(1) + "");

		int totalBlobFilter = 0;

		ArrayList<Integer> blobFilter = new ArrayList<Integer>();
		for (int i = 0; i < jumPixelBlob.size(); i++) {
			if (jumPixelBlob.get(i) < batas) {
				blobFilter.add(i + 1);
			} else {
				totalBlobFilter += jumPixelBlob.get(i);
			}
		}
//		Log.d("width in", "" + label.length);
//		Log.d("Height in", "" + label[0].length);
//		Log.d("jumlah blob filter", "" + totalBlobFilter);
//		Log.d("batas alarm", "" + (label.length * label[0].length) * 0.3);

		for (int j = 0; j < label.length; j++) {
			for (int k = 0; k < label[0].length; k++) {
				if (label[j][k] != 0) {
					for (int i = 0; i < blobFilter.size(); i++) {
						if (label[j][k] == blobFilter.get(i)) {
							in.setPixel(lenghtLineX + 1 + j, lenghtLineY + 1
									+ k, 0);
						}
					}
				}
			}
		}

		return totalBlobFilter;
	}
}
