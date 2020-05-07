package com.iwstars.mcnes.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * @description: 渲染controller
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesRenderController {

    @FXML
    private Canvas mainCanvas;

    public void render(short[][] pixelColorBuff){
        GraphicsContext graphics = mainCanvas.getGraphicsContext2D();
        PixelWriter pixelWriter = graphics.getPixelWriter();
        for(int h=0; h<240; h++) {
            for(int w=0;w<256;w++) {
                short[] i = pixelColorBuff[w + (h*240)];
                pixelWriter.setColor(w,h,Color.rgb(i[0],i[1],i[2]));
            }
        }
    }
}
