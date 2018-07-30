package com.makvenis.hotel.listhotol;

/* EventBus广播价格改变事件 */
public class SaleMessage {

    public String sale;
    public String cid;

    public SaleMessage(String sale, String cid) {
        this.sale = sale;
        this.cid = cid;
    }

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    @Override
    public String toString() {
        return "SaleMessage{" +
                "sale='" + sale + '\'' +
                ", cid='" + cid + '\'' +
                '}';
    }
}
