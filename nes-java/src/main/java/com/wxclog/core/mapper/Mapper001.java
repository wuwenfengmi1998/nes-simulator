package com.wxclog.core.mapper;

import com.wxclog.core.DataBus;

/**
 * Mapper #1
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=MMC1">INES_Mapper_001</a>
 */
public class Mapper001 implements IMapper {

    private byte switchBank = 0,switchCHRBank_0 = -1,switchCHRBank_1 = -1;
    private byte romPRGSize;
    private byte[] cardChrBank;

    private byte times,regSR;

    private byte mirroring,prgModel = -1,chrModel = -1;

    public Mapper001(byte romPRGSize, byte romChrSize, byte[] romCHR) {
        this.romPRGSize = romPRGSize;
        if(romChrSize > 1){
            cardChrBank = new byte[(romChrSize-1)*8*1024];
            System.arraycopy(romCHR,1*8*1024,cardChrBank,0,cardChrBank.length);
        }
    }

    @Override
    public void write(int addr, byte data) {
        if(addr>=0x8000 & addr<=0xFFFF){
            if((data&0x80) == 0x80){
                regSR = times = 0;
                return;
            }
            byte last;
            if(addr>=0x8000&&addr<=0x9FFF){
                last = 1;
//                System.out.println("Control");
            }else if(addr>=0xA000&&addr<=0xBFFF){
                last = 2;
//                System.out.println("CHR bank 0");
            }else if(addr>=0xC000&&addr<=0xDFFF){
                last = 3;
//                System.out.println("CHR bank 1");
            }else{
                last = 4;
//                System.out.println("PRG bank");
            }
            regSR|=((data&1)<<times);
            times++;
            if(times == 5){
                switch (last){
                    case 1:
                        mirroring = (byte) (regSR&0x3);
                        prgModel = (byte) ((regSR>>2)&0x3);
                        chrModel = (byte) ((regSR>>4)&0x1);
                        break;
                    case 2:
                        switchCHRBank_0 = regSR;
                        switchCHRBank_1 = -1;
                        break;
                    case 3:
                        if(chrModel == 1){
                            switchCHRBank_1 = regSR;
                        }
                        switchCHRBank_0 = -1;
                        break;
                    case 4:
                        switchBank = (byte) (regSR&0xFF);
                        break;
                }
                regSR = times = 0;
            }
//            System.out.println("Data bit:"+(data&1)+" Reset shift:"+((data>>7)&1));
            return;
        }
        DataBus.cpuMemory.writeMem(addr,data);
    }

    @Override
    public byte read(int addr) {
        if(addr>=0x8000 && addr<=0xFFFF){
            //32K
            if(prgModel==0||prgModel == 1){
                addr += switchBank*32*1024;
            }else if(prgModel == 2){
                //fix first bank at $8000 and switch 16 KB bank at $C000
                if(addr>=0xC000){
                    addr += switchBank*16*1024;
                }
            }else{
                // fix last bank at $C000 and switch 16 KB bank at $8000)
                if(addr>=0xC000 && addr<=0xFFFF){
                    //偏移量
                    addr -= 0xC000;
                    addr += 0x8000+(romPRGSize-1)*16*1024;
                }else if(addr>=0x8000 && addr<=0xBFFF && switchBank>=0){
                    addr += switchBank*16*1024;
                }
            }
        }
        return DataBus.cpuMemory.readMem(addr);
    }

    @Override
    public void writePpu(int addr, byte data) {
        DataBus.ppuMemory.write(addr,data);
    }

    @Override
    public byte readPpu(int addr) {
        if((switchCHRBank_0 != -1||switchCHRBank_1 != -1) && addr < 0x2000) {
            //Select 4 KB or 8 KB CHR bank at PPU $0000 (low bit ignored in 8 KB mode)
            if(switchCHRBank_0 != -1 && addr < 0x1000){

                if(switchCHRBank_0 != 0){
                    //switch 8 KB at a time
                    if(chrModel == 0){
                        return cardChrBank[addr+switchCHRBank_0*8*1024];
                    }else{
                        //1: switch two separate 4 KB banks
                        return cardChrBank[addr+switchCHRBank_0*4*1024];
                    }
                }


            }else if(switchCHRBank_1 != -1 && addr >= 0x1000) {
                if(switchCHRBank_0 != 0){
                    //1: switch two separate 4 KB banks
                    return cardChrBank[addr+switchCHRBank_1*4*1024];
                }
            }
        }
        return DataBus.ppuMemory.readMem(addr);
    }
}
