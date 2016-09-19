package srimobile.aspen.leidos.com.sri.blutooth;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;


import srimobile.aspen.leidos.com.sri.data.*;

/**
 * Created by cassadyja on 7/23/2015.
 */
public class DSRCCoder {

    public static WeightResultData decodeWeightResultData(byte[] bytes){
        WeightResultData data = new WeightResultData();
        String hex = OBUBluetoothService.bytesToHex(bytes);

        int index = 0;
        String tag = hex.substring(index, index+=2);
        String len = hex.substring(index, index+=2);
        int lenInt = Integer.parseInt(len, 16);
        int totalLen = lenInt;

        while (index < totalLen*2){
            tag = hex.substring(index, index+=2);
            len = hex.substring(index, index+=2);
            lenInt = Integer.parseInt(len, 16);
            String value = hex.substring(index, index + (lenInt*2));
            index += (lenInt*2);
            processWeightValue(data, tag, value, lenInt);
        }


        return data;
    }


    private static void processWeightValue(WeightResultData data, String tag, String value, int len){
        if(tag.equalsIgnoreCase("80")){
            data.setId(new String(new BigInteger(value, 16).toByteArray()));
        }
        if(tag.equalsIgnoreCase("81")){
            data.setSiteId(new String(new BigInteger(value, 16).toByteArray()));
        }
        if(tag.equalsIgnoreCase("A2")){
            data.setFullDate(decodeDateTime(value, len));
        }
        if(tag.equalsIgnoreCase("83")){
            data.setWeightResult(new String(new BigInteger(value, 16).toByteArray()));
        }
    }


    public static byte[] encodeVehicleData(DriverVehicleInformationData data){
        byte[] retBytes = null;
        ByteBuffer buff = ByteBuffer.allocate(500);
        byte[] val = null;

        //id 0
        if(data.getId() != null){
            buff.put(new byte[]{(byte)0x80});

            val = data.getId().getBytes();
            buff.put(new byte[]{(byte)val.length});
            buff.put(val);
        }


        //siteId 1
        buff.put(new byte[]{(byte)0x81});

        val = data.getSiteId().getBytes();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);


        //dl 2
        if(data.getDriversLicenseNumber() != null){
            buff.put(new byte[]{(byte)0x82});

            val = data.getDriversLicenseNumber().getBytes();
            buff.put(new byte[]{(byte)val.length});
            buff.put(val);

        }

        //cdl 3
        if(data.getCdlNumber() != null){
            buff.put(new byte[]{(byte)0x83});

            val = data.getCdlNumber().getBytes();
            buff.put(new byte[]{(byte)val.length});
            buff.put(val);

        }


        //vin 4
        buff.put(new byte[]{(byte)0x84});

        val = data.getVin().getBytes();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);

        //dot 5
        buff.put(new byte[]{(byte)0x85});

        val = data.getUsdotNumber().getBytes();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);

        //plate 6
        buff.put(new byte[]{(byte)0x86});

        val = data.getPlateNumber().getBytes();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);

        // lat 7
        buff.put(new byte[]{(byte)0x87});

        val = new BigInteger(Integer.toString(data.getLat().getValue())).toByteArray();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);

        //lon 8
        buff.put(new byte[]{(byte)0x88});

        val = new BigInteger(Integer.toString(data.getLon().getValue())).toByteArray();
        buff.put(new byte[]{(byte)val.length});
        buff.put(val);

        //date 9
        buff.put(encodeDateTime(data.getFullDate()));




        ByteBuffer ret = ByteBuffer.allocate(1000);
        int size = buff.position();
        val = buff.array();
        byte[] resize = Arrays.copyOf(val, size);
        ret.put(new byte[]{(byte)0x30,(byte)resize.length});
        ret.put(resize);

        size = ret.position();
        val = ret.array();
        resize = Arrays.copyOf(val, size);
        return resize;
    }

    private static byte[] encodeDateTime(DDateTime dateTime){
        byte[] retBytes = null;
        ByteBuffer buff = ByteBuffer.allocate(300);
        buff.put(new byte[]{(byte)0xA9});

        ByteBuffer buff2 = ByteBuffer.allocate(80);
        byte[] val = null;

        buff2.put(new byte[]{(byte)0x80});
        val = new BigInteger(Integer.toString(dateTime.getYear().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        buff2.put(new byte[]{(byte)0x81});
        val = new BigInteger(Integer.toString(dateTime.getMonth().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        buff2.put(new byte[]{(byte)0x82});
        val = new BigInteger(Integer.toString(dateTime.getDay().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        buff2.put(new byte[]{(byte)0x83});
        val = new BigInteger(Integer.toString(dateTime.getHour().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        buff2.put(new byte[]{(byte)0x84});
        val = new BigInteger(Integer.toString(dateTime.getMinute().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        buff2.put(new byte[]{(byte)0x85});
        val = new BigInteger(Integer.toString(dateTime.getSecond().getValue())).toByteArray();
        buff2.put(new byte[]{(byte)val.length});
        buff2.put(val);

        int size = buff2.position();
        val = buff2.array();
        byte[] resize = Arrays.copyOf(val, size);
        buff.put(new byte[]{(byte)resize.length});
        buff.put(resize);

        size = buff.position();
        resize = Arrays.copyOf(buff.array(), size);


        return resize;
    }

    public static DriverVehicleInformationData decodeVehicleData(byte[] bytes){
        DriverVehicleInformationData data = new DriverVehicleInformationData();
        String hex = OBUBluetoothService.bytesToHex(bytes);
        int index = hex.indexOf("80");
        if(index > -1){
            String totLen = hex.substring(2,4);
            int totLenInt = new BigInteger(totLen,16).intValue();
            while(index < (totLenInt*2)){
                String tag = hex.substring(index, index+=2);
                String len = hex.substring(index, index+2);
                index +=2;
                int lenInt = new BigInteger(len, 16).intValue();
                String value = hex.substring(index, index+(lenInt*2));
                Object val = null;
                index +=(lenInt*2);
                if(tag.equals("87")||tag.equals("88")){
                    val = new Integer(new BigInteger(value,16).intValue());
                }else if(tag.equalsIgnoreCase("A9")){
                    val = null;
                    DDateTime dateTime = decodeDateTime(value, lenInt);
                    data.setFullDate(dateTime);
                }else{
                    val = new String(new BigInteger(value, 16).toByteArray());

                }
                if(val != null){
                    setValue(data, tag, val);
                }
            }

        }
        return data;
    }


    private static DDateTime decodeDateTime(String hex, int totLen){
        int index = 0;
        DDateTime dateTime = new DDateTime();
        while(index < totLen*2){
            String tag = hex.substring(index, index+=2);
            String len = hex.substring(index, index+=2);
            int lenInt = new BigInteger(len, 16).intValue();
            String value = hex.substring(index, index+(lenInt*2));
            int valInt = new BigInteger(value, 16).intValue();
            setDateValue(dateTime, tag, valInt);
            index += (lenInt*2);
        }
        return dateTime;
    }

    private static void setDateValue(DDateTime dateTime, String tag, int value){

        if(tag.equals("80")){
            DYear year = new DYear(value);
            dateTime.setYear(year);
        }
        if(tag.equals("81")){
            DMonth month = new DMonth(value);
            dateTime.setMonth(month);
        }
        if(tag.equals("82")){
            DDay day = new DDay(value);
            dateTime.setDay(day);
        }
        if(tag.equals("83")){
            DHour hour = new DHour(value);
            dateTime.setHour(hour);
        }
        if(tag.equals("84")){
            DMinute minute = new DMinute(value);
            dateTime.setMinute(minute);

        }
        if(tag.equals("85")){
            DSecond second = new DSecond(value);
            dateTime.setSecond(second);
        }
    }

    private static void setValue(DriverVehicleInformationData data, String tag, Object value){
        if(tag.equals("80")){
            data.setId((String)value);
        }
        if(tag.equals("81")){
            data.setSiteId((String)value);
        }
        if(tag.equals("82")){
            data.setDriversLicenseNumber((String)value);
        }
        if(tag.equals("83")){
            data.setCdlNumber((String)value);
        }
        if(tag.equals("84")){
            data.setVin((String)value);
        }
        if(tag.equals("85")){
            data.setUsdotNumber((String)value);
        }
        if(tag.equals("86")){
            data.setPlateNumber((String)value);
        }
        if(tag.equals("87")){
            Latitude lat = new Latitude((Integer)value);
            data.setLat(lat);
        }
        if(tag.equals("88")){
            Longitude lon = new Longitude((Integer)value);
            data.setLon(lon);
        }



    }

}
