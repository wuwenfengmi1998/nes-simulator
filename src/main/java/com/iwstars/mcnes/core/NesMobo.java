package com.iwstars.mcnes.core;

import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.cpu.CpuRegister;
import com.iwstars.mcnes.core.ppu.Ppu;
import lombok.Setter;

/**
 * @description: nes模拟板
 * @author: WStars
 * @date: 2020-04-19 10:06
 */
@Setter
public class NesMobo {

    /**
     * CPU
     */
    private Cpu6502 cpu6502;

    /**
     * PPU图形处理
     */
    private Ppu ppu;

    /**
     * 主板通电
     */
    public void powerUp(){
        while (true)  {
            //256x240 分辨率
            //设置vblank false
            DataBus.p_2002[7] = 0;
            for (int i = 0; i < 262; i++) {
                //HBlank start
                if(i<240) {
                    System.out.println("");
                    System.out.println("");
                    ppu.startRender();
                }
                this.cpu6502.go();
                //VBlank
                if(i==241) {
                    DataBus.p_2002[7] = 1;
                    if(DataBus.p_2000[7] == 1) {
                        CpuRegister.NMI(cpu6502.getCpuMemory());
                    }
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断电
     */
    public void powerDown(){

    }

    /**
     * 复位
     */
    public void reset(){

    }

    public static void main(String[] args) {
        System.out.println(-127&0xff);
    }
}
