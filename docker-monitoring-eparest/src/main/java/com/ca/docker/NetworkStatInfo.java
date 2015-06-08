package com.ca.docker;

public class NetworkStatInfo
{
    Long bytesreceived ;
    Long packetsreceived ;
    Long errorsreceived ;
    Long dropsreceived ;
    Long bytestransmitted ;
    Long packetstransmitted;
    Long errorstransmitted ;
    Long dropstransmitted ;
    public NetworkStatInfo()
    {
        bytesreceived=(long) 0;
        packetsreceived=(long) 0;
        errorsreceived=(long) 0;
        dropsreceived=(long) 0;
        bytestransmitted=(long) 0;
        packetstransmitted=(long) 0;
        errorstransmitted=(long) 0;
        dropstransmitted=(long) 0;
        
    }
    public Long getBytesreceived()
    {
        return bytesreceived;
    }
    public Long getPacketsreceived()
    {
        return packetsreceived;
    }
    public Long getErrorsreceived()
    {
        return errorsreceived;
    }
    public Long getDropsreceived()
    {
        return dropsreceived;
    }
    public Long getBytestransmitted()
    {
        return bytestransmitted;
    }
    public Long getPacketstransmitted()
    {
        return packetstransmitted;
    }
    public Long getErrorstransmitted()
    {
        return errorstransmitted;
    }
    public Long getDropstransmitted()
    {
        return dropstransmitted;
    }
    public void setBytesreceived(Long bytesreceived)
    {
        this.bytesreceived = bytesreceived;
    }
    public void setPacketsreceived(Long packetsreceived)
    {
        this.packetsreceived = packetsreceived;
    }
    public void setErrorsreceived(Long errorsreceived)
    {
        this.errorsreceived = errorsreceived;
    }
    public void setDropsreceived(Long dropsreceived)
    {
        this.dropsreceived = dropsreceived;
    }
    public void setBytestransmitted(Long bytestransmitted)
    {
        this.bytestransmitted = bytestransmitted;
    }
    public void setPacketstransmitted(Long packetstransmitted)
    {
        this.packetstransmitted = packetstransmitted;
    }
    public void setErrorstransmitted(Long errorstransmitted)
    {
        this.errorstransmitted = errorstransmitted;
    }
    public void setDropstransmitted(Long dropstransmitted)
    {
        this.dropstransmitted = dropstransmitted;
    }
    
    

}
