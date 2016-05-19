package api.business.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "moneytransactions", schema = "payment", catalog = "clubby")
public class MoneyTransaction {
    private String transactionid;
    private int status;
    private Date creationTime;
    private int transactionTypeId;
    private String currency;
    private int amount;

    @Id
    @Column(name = "transactionid")
    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    @Basic
    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Basic
    @Column(name = "creationTime")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Basic
    @Column(name = "transactionTypeId")
    public int getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(int transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    @Basic
    @Column(name = "currency")
    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Basic
    @Column(name = "amount")
    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoneyTransaction that = (MoneyTransaction) o;

        if (transactionid != null ? !transactionid.equals(that.transactionid) : that.transactionid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transactionid != null ? transactionid.hashCode() : 0;
        return result;
    }

    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "paymentid", referencedColumnName = "paymentid")
    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userid", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
