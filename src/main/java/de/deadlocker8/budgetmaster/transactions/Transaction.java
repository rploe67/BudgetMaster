package de.deadlocker8.budgetmaster.transactions;

import com.google.gson.annotations.Expose;
import de.deadlocker8.budgetmaster.tags.Tag;
import de.deadlocker8.budgetmaster.accounts.Account;
import de.deadlocker8.budgetmaster.categories.Category;
import de.deadlocker8.budgetmaster.repeating.RepeatingOption;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Transaction
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Expose
	private Integer ID;
	@Expose
	private Integer amount;

	@DateTimeFormat(pattern = "dd.MM.yyyy")
	@Expose
	private DateTime date;

	@ManyToOne
	@Expose
	private Account account;

	@ManyToOne
	@Expose
	private Category category;

	@Expose
	private String name;
	@Expose
	private String description;

	@ManyToMany
	@Expose
	@JoinTable(
			name = "transaction_tags",
			joinColumns = @JoinColumn(
					name = "transaction_id", referencedColumnName = "ID"),
			inverseJoinColumns = @JoinColumn(
					name = "tags_id", referencedColumnName = "ID"))
	private List<Tag> tags;

	@ManyToOne(optional = true, cascade = CascadeType.ALL)
	@Expose
	private RepeatingOption repeatingOption;

	@OneToOne(optional = true)
	@Expose
	private Account transferAccount;

	public Transaction()
	{
	}

	public Transaction(Transaction transaction)
	{
		this.ID = transaction.getID();
		this.amount = transaction.getAmount();
		this.date = transaction.getDate();
		this.account = transaction.getAccount();
		this.category = transaction.getCategory();
		this.name = transaction.getName();
		this.description = transaction.getDescription();
		this.tags = new ArrayList<>(transaction.getTags());
		this.repeatingOption = transaction.getRepeatingOption();
		this.transferAccount = transaction.getTransferAccount();
	}

	public Integer getID()
	{
		return ID;
	}

	public void setID(Integer ID)
	{
		this.ID = ID;
	}

	public Integer getAmount()
	{
		return amount;
	}

	public void setAmount(Integer amount)
	{
		this.amount = amount;
	}

	public DateTime getDate()
	{
		return date;
	}

	public void setDate(DateTime date)
	{
		this.date = date;
	}

	public Account getAccount()
	{
		return account;
	}

	public void setAccount(Account account)
	{
		this.account = account;
	}

	public Category getCategory()
	{
		return category;
	}

	public void setCategory(Category category)
	{
		this.category = category;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<Tag> getTags()
	{
		return tags;
	}

	public void setTags(List<Tag> tags)
	{
		this.tags = tags;
	}

	public RepeatingOption getRepeatingOption()
	{
		return repeatingOption;
	}

	public void setRepeatingOption(RepeatingOption repeatingOption)
	{
		this.repeatingOption = repeatingOption;
	}

	public boolean isRepeating()
	{
		return repeatingOption != null;
	}

	public Account getTransferAccount()
	{
		return transferAccount;
	}

	public void setTransferAccount(Account transferAccount)
	{
		this.transferAccount = transferAccount;
	}

	public boolean isTransfer()
	{
		return transferAccount != null;
	}

	public boolean isFuture()
	{
		return date.isAfter(DateTime.now());
	}

	@Override
	public String toString()
	{
		String value = "Transaction{" +
				"ID=" + ID +
				", amount=" + amount +
				", date=" + date +
				", account=Account[ID=" + account.getID() + ", name=" + account.getName() + "]" +
				", category=" + category +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", tags=" + tags +
				", repeatingOption=" + repeatingOption;
		if(transferAccount == null)
		{
			value += ", transferAccount=null";
		}
		else
		{
			value += ", transferAccount=Account[ID=" + transferAccount.getID() + ", name=" + transferAccount.getName() + "]";
		}

		value += '}';
		return value;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		Transaction transaction = (Transaction) o;
		return Objects.equals(ID, transaction.ID) &&
				Objects.equals(amount, transaction.amount) &&
				Objects.equals(date, transaction.date) &&
				Objects.equals(account, transaction.account) &&
				Objects.equals(category, transaction.category) &&
				Objects.equals(name, transaction.name) &&
				Objects.equals(description, transaction.description) &&
				Objects.equals(tags, transaction.tags) &&
				Objects.equals(repeatingOption, transaction.repeatingOption) &&
				Objects.equals(transferAccount, transaction.transferAccount);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ID, amount, date, account, category, name, description, tags, repeatingOption, transferAccount);
	}
}
