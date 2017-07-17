//20140703 - Jerome Parent
//Reader for Koala binary generated by Lyncee Tec software
//This plugin read .bin file, scale the XY value in microns and the Z value in nm
//Based on reader tutorial provided by Albert Cardona : http://albert.rierol.net/imagej_programming_tutorials.html

import ij.*;
import ij.io.*;
import ij.plugin.*;
import ij.process.*;
import java.io.*;
import java.nio.*;


public class Koala_Bin_Reader extends ImagePlus implements PlugIn {  
	
	public void run(String arg) {  
        String path = getPath(arg);  
        if (null == path) return;  
        if (!parse(path)) return;  
        if (null == arg || 0 == arg.trim().length()) this.show(); // was opened by direct call to the plugin  
                          // not via HandleExtraFileTypes which would  
                          // have given a non-null arg.  
    }
  
    /** Accepts URLs as well. */  
    private String getPath(String arg) {  
    	if (null != arg) {  
            if (0 == arg.indexOf("http://")  
             || new File(arg).exists()) return arg;  
        }  
        // else, ask:  
        OpenDialog od = new OpenDialog("Choose a .bin file", null);  
        String dir = od.getDirectory();  
        if (null == dir) return null; // dialog was canceled  
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")) dir += "/";  
        return dir + od.getFileName();  
    }  
  
    /** Opens URLs as well. */  
    private InputStream open(String path) throws Exception {  
        if (0 == path.indexOf("http://"))  
            return new java.net.URL(path).openStream();  
        return new FileInputStream(path);  
    }  
  
    private boolean parse(String path) {  
        // Open file and read header 
		// header size 23 bytes
		byte[] buf = new byte[23];  
        try {  
            InputStream stream = open(path);  
            stream.read(buf, 0, 23);  
            stream.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }
			byte version = buf[0];
			byte endianness = buf[1];
			int header_size = readIntLittleEndian(buf,2);
			int width = readIntLittleEndian(buf,6);  
			int height = readIntLittleEndian(buf,10);
			float pixel_size = readFloatLittleEndian(buf,14);
			float height_conv = readFloatLittleEndian(buf,18);
			byte unit = buf[22];	
			
        // Build a new FileInfo object with all file format parameters and file data  
        FileInfo fi = new FileInfo();
        //header information is store in fileInfo debugInfo
        String header =  "Koala binary file header information" + System.lineSeparator();
        header += "Version : "+ version + System.lineSeparator();
        header += "Endianness : " + endianness + System.lineSeparator();
        header += "Header size : " + header_size + System.lineSeparator();
        header += "Width : " + width + ", height : " + height + System.lineSeparator();
        header += "Pixel size : " + pixel_size + System.lineSeparator();
        header += "Height_conv : " + height_conv+ System.lineSeparator();
        header += "Unit : " + unit;
        fi.debugInfo = header;
        fi.fileType = fi.GRAY32_FLOAT;  
        fi.fileFormat = fi.RAW;  
        int islash = path.lastIndexOf('/');  
        if (0 == path.indexOf("http://")) {  
            fi.url = path;  
        } else {  
            fi.directory = path.substring(0, islash+1);  
        }  
        fi.fileName = path.substring(islash+1);
		fi.description = "Koala binary file";
        fi.width = width;  
        fi.height = height;
		// conversion XY in microns
		fi.pixelHeight = (double)pixel_size*1e6;
		fi.pixelWidth = (double)pixel_size*1e6;
		fi.unit = "um";
		//Set Unit
		if (unit > 0) {// only for phase image
			fi.pixelDepth = height_conv;
			if(Math.round(height_conv) == Math.round(180/Math.PI)) {
				fi.valueUnit = Character.toString((char)176); //°
			}
			else {
				fi.valueUnit = "nm";
			}
		}
		else {
			fi.pixelDepth = 0;
		}
		
        fi.nImages = 1;  
        fi.gapBetweenImages = 0;  
		if(endianness == 0)
			fi.intelByteOrder = true; // little endian 
		else	
			fi.intelByteOrder = false;
        fi.whiteIsZero = false; // no inverted LUT  
        fi.longOffset = fi.offset = header_size; // header size, in bytes  
        
        //add header information to description field -> not used 
        fi.description = "version : "+version+", endianness : "+endianness+", unit : "+unit+", h_conv : "+height_conv;
  
        // Now make a new ImagePlus out of the FileInfo  
        // and integrate its data into this PlugIn, which is also an ImagePlus  
        try {  
            FileOpener fo = new FileOpener(fi);  
            ImagePlus imp = fo.open(false); 
			ImageProcessor temp = imp.getProcessor();
			//set vertical scale to phase image
			if (unit > 0){	// conversion in nm
				if(Math.round(height_conv) == Math.round(180/Math.PI)) {
					temp.multiply(height_conv); //-> conversion in deg (phase vibation map)
				}
				else {
					temp.multiply(height_conv*1e9); //converison in nm
				}
				imp.setProcessor(temp);
			}
            this.setStack(imp.getTitle(), imp.getStack());  
            this.setCalibration(imp.getCalibration());  
            Object obinfo = imp.getProperty("Info");  
            if (null != obinfo) this.setProperty("Info", obinfo);  
            this.setFileInfo(imp.getOriginalFileInfo());
			imp.close();
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        } 
        return true;  
    }
	private final int readIntLittleEndian(byte[] buf, int start) { 
		byte temp[] = new byte[4];
		for (int i=0;i<4;i++)			temp[i] = buf[start+i];
		return ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN ).getInt(); 
    }  
	private final float readFloatLittleEndian(byte[] buf, int start) {
		byte temp[] = new byte[4];
		for (int i=0;i<4;i++)
			temp[i] = buf[start+i];
		return ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN ).getFloat();
	}
}  