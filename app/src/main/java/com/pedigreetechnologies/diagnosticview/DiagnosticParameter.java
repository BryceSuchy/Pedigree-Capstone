package com.pedigreetechnologies.diagnosticview;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joe on 2/16/2017.
 */

public class DiagnosticParameter implements Comparable<DiagnosticParameter>, Parcelable {

    public static final Creator<DiagnosticParameter> CREATOR = new Creator<DiagnosticParameter>() {
        @Override
        public DiagnosticParameter createFromParcel(Parcel in) {
            return new DiagnosticParameter(in);
        }

        @Override
        public DiagnosticParameter[] newArray(int size) {
            return new DiagnosticParameter[size];
        }
    };
    private int dataType;
    private int parameterID;
    private int sourceAddress;
    private int startByte;
    private int startBit;
    private int bitLength;
    private boolean endianness;
    private long errorValue;
    private double resolution;
    private double offset;
    private double min;
    private double max;
    private String units;
    private String type;
    private String label;

    public DiagnosticParameter(int dataType, int parameterID, int sourceAddress, int startByte, int startBit, int bitLength, boolean endianness, long errorValue, double resolution, double offset, double min, double max, String units, String type, String label) {
        this.dataType = dataType;
        this.parameterID = parameterID;
        this.sourceAddress = sourceAddress;
        this.startByte = startByte;
        this.startBit = startBit;
        this.bitLength = bitLength;
        this.endianness = endianness;
        this.errorValue = errorValue;
        this.resolution = resolution;
        this.offset = offset;
        this.min = min;
        this.max = max;
        this.units = units;
        this.type = type;
        this.label = label;
    }

    public DiagnosticParameter(Parcel parcel){
        this.dataType = parcel.readInt();
        this.parameterID = parcel.readInt();
        this.sourceAddress = parcel.readInt();
        this.startByte = parcel.readInt();
        this.startBit = parcel.readInt();
        this.bitLength = parcel.readInt();
        this.endianness = parcel.readByte() != 0;
        this.errorValue = parcel.readLong();
        this.resolution = parcel.readDouble();
        this.offset = parcel.readDouble();
        this.min = parcel.readDouble();
        this.max = parcel.readDouble();
        this.units = parcel.readString();
        this.type = parcel.readString();
        this.label = parcel.readString();
    }

    public static DiagnosticParameter createObject(String[] csvSplitString) throws CSVParseError {
        if(csvSplitString.length < 15){
            throw new CSVParseError("The Number of parameters read from the CSV file are less than the required information");
        }
        else if(csvSplitString.length > 15){
            throw new CSVParseError("The Number of parameters read from the CSV file are more than the required information");
        }
        int dataType = Integer.parseInt(csvSplitString[0]);
        int parameterID = Integer.parseInt(csvSplitString[1]);
        int sourceAddress = Integer.parseInt(csvSplitString[2]);
        int startByte = Integer.parseInt(csvSplitString[3]);
        int startBit = Integer.parseInt(csvSplitString[4]);
        int bitLength = Integer.parseInt(csvSplitString[5]);
        boolean endianness;

        if(csvSplitString[6].equals("1")){
            endianness = true;
        }
        else{
            endianness = false;
        }
        long errorValue = Long.parseLong(csvSplitString[7]);
        double resolution = Double.parseDouble(csvSplitString[8]);
        double offset = Double.parseDouble(csvSplitString[9]);

        double min;
        if(csvSplitString[10].isEmpty()){
            min = Float.NaN;
        }
        else {
            min = Double.parseDouble(csvSplitString[10]);
        }

        double max;
        if(csvSplitString[11].isEmpty()){
            max = Float.NaN;
        }
        else {
            max = Double.parseDouble(csvSplitString[11]);
        }

        String units = csvSplitString[12];
        String type = csvSplitString[13];
        String label = csvSplitString[14];
        return new DiagnosticParameter(dataType, parameterID, sourceAddress, startByte, startBit, bitLength, endianness, errorValue, resolution, offset, min, max, units, type, label);
    }

    public int getDataType() {
        return dataType;
    }

    public int getParameterID() {
        return parameterID;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public int getStartByte() {
        return startByte;
    }

    public int getStartBit() {
        return startBit;
    }

    public int getBitLength() {
        return bitLength;
    }

    public boolean isEndianness() {
        return endianness;
    }

    public long getErrorValue() {
        return errorValue;
    }

    public double getResolution() {
        return resolution;
    }

    public double getOffset() {
        return offset;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getUnits() {
        return units;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int compareTo(DiagnosticParameter diagnosticParameter) {
        if(dataType - diagnosticParameter.getDataType() <= 0){
            if(parameterID - diagnosticParameter.getParameterID() < 0){
                return -1;
            }
            else if(parameterID - diagnosticParameter.getParameterID() == 0){
                return 0;
            }
            else{
                return 1;
            }
        }
        else{
            return 1;
        }
    }

    public float calcMeasurement(String message){

        try{
            //Creates the shortened message that applies to the parameter
            int startIndex = 2 * startByte;
            int endIndex = startIndex + bitLength / 4;
            String shortenedMessage = message.substring(startIndex, endIndex);

            long messageValue;

            if (endianness == false) {
                int stringLength = shortenedMessage.length();
                char[] littleEndian = new char[stringLength];

                for (int i = 0; i < stringLength / 2; i++) {
                    littleEndian[2 * i] = shortenedMessage.charAt(stringLength - ((2 * i) + 2));
                    littleEndian[(2 * i) + 1] = shortenedMessage.charAt(stringLength - ((2 * i) + 2) + 1);
                }

                messageValue = Long.parseLong(new String(littleEndian), 16);
            } else {
                messageValue = Long.parseLong(shortenedMessage, 16);
            }

            //if the message an value are the same return NAN to signify it is erroneous could be changed to an exception
            if(messageValue == errorValue){
                return Float.NaN;
            }
            float messageCalculatedValue = (float)(((messageValue * resolution) + offset));
            return messageCalculatedValue;
        }
        catch (Exception e){
            e.printStackTrace();
            return Float.NaN;
        }
    }

    @Override
    public String toString() {
        return "DiagnosticParameter{" +
                "dataType=" + dataType +
                ", parameterID=" + parameterID +
                ", sourceAddress=" + sourceAddress +
                ", startByte=" + startByte +
                ", startBit=" + startBit +
                ", bitLength=" + bitLength +
                ", endianness=" + endianness +
                ", errorValue=" + errorValue +
                ", resolution=" + resolution +
                ", offset=" + offset +
                ", min=" + min +
                ", max=" + max +
                ", units='" + units + '\'' +
                ", type='" + type + '\'' +
                ", label='" + label + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DiagnosticParameter)){
            return false;
        }

        DiagnosticParameter parameter = (DiagnosticParameter)obj;

        return this.dataType == parameter.dataType && this.parameterID == parameter.parameterID
                && this.sourceAddress == parameter.sourceAddress
                && this.startByte == parameter.startByte && this.startBit == parameter.startBit
                && this.bitLength == parameter.bitLength && this.endianness == parameter.endianness
                && this.errorValue == parameter.errorValue
                && this.resolution == parameter.resolution && this.offset == parameter.offset
                && this.min == parameter.min && this.max == parameter.max
                && this.units.equals(parameter.units) && this.type.equals(parameter.type)
                && this.label.equals(parameter.label);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(dataType);
        parcel.writeInt(parameterID);
        parcel.writeInt(sourceAddress);
        parcel.writeInt(startByte);
        parcel.writeInt(startBit);
        parcel.writeInt(bitLength);
        parcel.writeByte((byte) (endianness ? 1 : 0));
        parcel.writeLong(errorValue);
        parcel.writeDouble(resolution);
        parcel.writeDouble(offset);
        parcel.writeDouble(min);
        parcel.writeDouble(max);
        parcel.writeString(units);
        parcel.writeString(type);
        parcel.writeString(label);
    }

    public static class CSVParseError extends Exception {

        public CSVParseError(String message) {
            super(message);
        }
    }
}
