package il.co.codeguru.corewars8086.hardware.memory;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;
import il.co.codeguru.corewars8086.hardware.Device;
import il.co.codeguru.corewars8086.hardware.Machine;
import il.co.codeguru.corewars8086.hardware.Storable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of the RealModeMemory interface which limits memory access
 * to given regions of the memory.
 *
 * @author DL
 * @author Erdem Guven
 */
public class MemoryAccessProtection implements MemoryAccessListener, Storable, Device {

	static final int MAX_REGION_COUNT = 100;

	public static final int PROT_NONE  = 0;
	public static final int PROT_READ  = 1;
	public static final int PROT_WRITE = 2;
	public static final int PROT_READ_WRITE  = 3;
	public static final int PROT_EXEC  = 4;
	public static final int PROT_ALL   = 7;

	int regionEnd[] = new int[MAX_REGION_COUNT];
	byte regionProt[] = new byte[MAX_REGION_COUNT];
	int regionCount = 0;

	//private boolean protection = false;

	/**
	 * Constructor.
	 */

	public MemoryAccessProtection() {
		reset();
	}

	public void readMemory(AbstractAddress address, int size) throws MemoryException {
		// 	is reading allowed from this address ?
		if (getProtection(address,size,PROT_READ) != PROT_READ) {
			throw new MemoryException("Non readable memory location: "+size+" bytes at "+address);
		}
	}

	public void readExecuteMemory(AbstractAddress address, int size) throws MemoryException {
		// 	is execution allowed from this address ?
		if (getProtection(address,size,PROT_EXEC) != PROT_EXEC) {
			throw new MemoryException("Non executable memory location: "+size+" bytes at "+address);
		}
	}

	public void writeMemory(AbstractAddress address, int size) throws MemoryException {
		//is writing allowed to this address ?
		if (getProtection(address,size,PROT_WRITE) != PROT_WRITE) {
			throw new MemoryException("Non writable memory location: "+size+" bytes at "+address);
		}
	}

	public void setMachine(Machine mac) {
	}

	public void reset(){
		regionEnd[0]=0;
		regionProt[0]=-1;
		regionCount=1;
	}

	public int getProtection(AbstractAddress startAddr, int size, int prot) {
		//if(!protection){
		//	return prot;
		//}

		int start = startAddr.getLinearAddress();
		int end = start + size;
		if(end>startAddr.getMaxAddr()){
			end = startAddr.getMaxAddr();
			size = end - start;
		}

		if(size<=0){
			return prot;
		}

		int a,b;
		for(a=0; a<regionCount && start>=regionEnd[a]; a++);
		if(a>=regionCount){
			return PROT_NONE;
		}

		prot &= regionProt[a];
		for(b=a; b<regionCount && end>regionEnd[b]; b++){
			prot &= regionProt[b];
		}

		return prot;
	}

	public void setProtection(AbstractAddress startAddr, int size, int prot) {
		int start = startAddr.getLinearAddress();
		int end = start + size;
		if(end>startAddr.getMaxAddr()){
			end = startAddr.getMaxAddr();
		}
		size = end - start;

		if(size<=0){
			return;
		}

		int a,b;
		for(a=0; a<regionCount && start>regionEnd[a]; a++);
		if(a<regionCount && regionProt[a] == prot){
			if(a>0){
				start=regionEnd[a-1];
			} else {
				start = 0;
			}
			a--;
		}

		for(b=a; b<regionCount && end>=regionEnd[b]; b++);
		if(b<regionCount && regionProt[b] == prot){
			end=regionEnd[b];
			b++;
		}

		int i = a;
		int c = 2 - (b-a);

		if( b<regionCount && end==regionEnd[b] ){
			c--;
		}

		if(regionCount + c > MAX_REGION_COUNT){
			return;
		}

		if( i < regionCount) {
			int i2 = i+2-c;
			if(i2!=i+2 && regionCount-i2 > 0){
				System.arraycopy(regionEnd, i2, regionEnd, i+2, regionCount-i2);
				System.arraycopy(regionProt, i2, regionProt, i+2, regionCount-i2);
			}
		} else {
			regionProt[i] = PROT_NONE;
		}

		regionEnd[i] = start;
		regionEnd[i+1] = end;
		regionProt[i+1] = (byte) prot;
		regionCount += c;
	}

	public int[] getRegion(int index) {
		if(index<regionCount){
			int start = index>0 ? regionEnd[index-1] : 0;
			return new int[]{start,regionEnd[index]-start,regionProt[index]};
		}
		return null;
	}

	public int getRegionCount() {
		return regionCount;
	}

	public void load(InputStream input) throws IOException {
		DataInputStream datain = new DataInputStream(input);
		int count = input.read();
		if( count > MAX_REGION_COUNT ){
			throw new IOException("More than max. timer.");
		}

		regionCount = count;
		for(int a=0; a< regionCount; a++){
			regionEnd[a] = datain.readInt();
			regionProt[a] = (byte) datain.read();
		}
	}

	public void save(OutputStream output) throws IOException {
		DataOutputStream dataout = new DataOutputStream(output);
		dataout.write(regionCount);

		for(int a=0; a< regionCount; a++){
			dataout.writeInt(regionEnd[a]);
			dataout.write(regionProt[a]);
		}
	}
}