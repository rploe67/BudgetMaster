package de.deadlocker8.budgetmaster.unit;

import de.deadlocker8.budgetmaster.accounts.Account;
import de.deadlocker8.budgetmaster.accounts.AccountRepository;
import de.deadlocker8.budgetmaster.accounts.AccountType;
import de.deadlocker8.budgetmaster.categories.Category;
import de.deadlocker8.budgetmaster.categories.CategoryRepository;
import de.deadlocker8.budgetmaster.categories.CategoryType;
import de.deadlocker8.budgetmaster.repeating.RepeatingOption;
import de.deadlocker8.budgetmaster.repeating.RepeatingOptionRepository;
import de.deadlocker8.budgetmaster.repeating.endoption.RepeatingEndAfterXTimes;
import de.deadlocker8.budgetmaster.repeating.modifier.RepeatingModifierDays;
import de.deadlocker8.budgetmaster.search.Search;
import de.deadlocker8.budgetmaster.tags.Tag;
import de.deadlocker8.budgetmaster.tags.TagRepository;
import de.deadlocker8.budgetmaster.transactions.Transaction;
import de.deadlocker8.budgetmaster.transactions.TransactionRepository;
import de.deadlocker8.budgetmaster.transactions.TransactionSearchSpecifications;
import de.deadlocker8.budgetmaster.transactions.TransactionSpecifications;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
public class TransactionSearchSpecificationsTest
{
	@Autowired
	private TransactionRepository transactionRepository;
	private Transaction transaction1;
	private Transaction transaction2;
	private Transaction repeatingTransaction;
	private Transaction transferTransaction;

	@Autowired
	private CategoryRepository categoryRepository;
	private Category category1;
	private Category category2;

	@Autowired
	private AccountRepository accountRepository;
	private Account account;
	private Account account2;

	@Autowired
	private TagRepository tagRepository;
	private Tag tag1;
	private Tag tag2;

	@Autowired
	private RepeatingOptionRepository repeatingOptionRepository;
	private RepeatingOption repeatingOption;

	@Before
	public void init()
	{
		account = accountRepository.save(new Account("TestAccount", AccountType.CUSTOM));
		account2 = accountRepository.save(new Account("TestAccount2", AccountType.CUSTOM));

		category1 = categoryRepository.save(new Category("Category1", "#ff0000", CategoryType.CUSTOM));
		category2 = categoryRepository.save(new Category("xxx", "#ff0000", CategoryType.CUSTOM));

		tag1 = tagRepository.save(new Tag("MyAwesomeTag"));
		tag2 = tagRepository.save(new Tag("TagMaster_2"));

		transaction1 = new Transaction();
		transaction1.setName("Test");
		transaction1.setAmount(200);
		transaction1.setDescription("Random Whatever");
		transaction1.setDate(new DateTime(2018, 10, 3, 12, 0, 0, 0));
		transaction1.setCategory(category1);
		transaction1.setAccount(account);
		ArrayList<Tag> tags = new ArrayList<>();
		tags.add(tag1);
		transaction1.setTags(tags);
		transaction1 = transactionRepository.save(transaction1);

		transaction2 = new Transaction();
		transaction2.setName("lalala");
		transaction2.setAmount(-525);
		transaction2.setDate(new DateTime(2018, 11, 3, 12, 0, 0, 0));
		transaction2.setCategory(category2);
		transaction2.setAccount(account);
		transaction2 = transactionRepository.save(transaction2);

		DateTime repeatingTransactionDate = DateTime.parse("2018-03-13", DateTimeFormat.forPattern("yyyy-MM-dd"));
		repeatingOption = new RepeatingOption();
		repeatingOption.setModifier(new RepeatingModifierDays(10));
		repeatingOption.setStartDate(repeatingTransactionDate);
		repeatingOption.setEndOption(new RepeatingEndAfterXTimes(2));
		repeatingOption = repeatingOptionRepository.save(repeatingOption);

		repeatingTransaction = new Transaction();
		repeatingTransaction.setAmount(-12300);
		repeatingTransaction.setDate(repeatingTransactionDate);
		repeatingTransaction.setCategory(category1);
		repeatingTransaction.setName("Repeating");
		repeatingTransaction.setDescription("");
		repeatingTransaction.setAccount(account);
		repeatingTransaction.setRepeatingOption(repeatingOption);
		ArrayList<Tag> tags2 = new ArrayList<>();
		tags2.add(tag2);
		repeatingTransaction.setTags(tags2);
		repeatingTransaction = transactionRepository.save(repeatingTransaction);

		transferTransaction = new Transaction();
		transferTransaction.setName("TransferTransaction");
		transferTransaction.setAmount(-500);
		transferTransaction.setDate(new DateTime(2018, 8, 3, 12, 0, 0, 0));
		transferTransaction.setCategory(category2);
		transferTransaction.setAccount(account);
		transferTransaction.setTransferAccount(account2);
		transferTransaction = transactionRepository.save(transferTransaction);
	}

	@Test
	public void getMatches_OnlyName()
	{
		Search search = new Search("Test", true, false, false, false, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_PartialName()
	{
		Search search = new Search("es", true, false, false, false, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_IgnoreCase()
	{
		Search search = new Search("tEST", true, true, true, true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_OnlyDescription()
	{
		Search search = new Search("What", true, true, true, true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_OnlyCategory()
	{
		Search search = new Search(category2.getName(), false, false, true, false, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertFalse(results.contains(transaction1));
		assertTrue(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertTrue(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_Order()
	{
		Search search = new Search("", true, true, true, true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		List<Transaction> expected = new ArrayList<>();
		expected.add(transaction2);
		expected.add(transaction1);
		expected.add(transferTransaction);
		expected.add(repeatingTransaction);

		assertEquals(expected, results);
	}

	@Test
	public void getMatches_Mixed()
	{
		Search search = new Search("e", true, true, true, true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertTrue(results.contains(repeatingTransaction));
		assertTrue(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_NoMatches()
	{
		Search search = new Search("asuzgdzasuiduzasds", true, true, true,true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertEquals(0, results.size());
	}

	@Test
	public void getMatches_SearchNothing()
	{
		Search search = new Search("egal", false, false, false,false, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertEquals(0, results.size());
	}

	@Test
	public void getMatches_SearchTagsEquals()
	{
		Search search = new Search("MyAwesomeTag", false, false, false,true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}

	@Test
	public void getMatches_SearchTagsLike()
	{
		Search search = new Search("Awesome", false, false, false,true, 0);
		Specification spec = TransactionSearchSpecifications.withDynamicQuery(search);

		List<Transaction> results = transactionRepository.findAll(spec);
		assertTrue(results.contains(transaction1));
		assertFalse(results.contains(transaction2));
		assertFalse(results.contains(repeatingTransaction));
		assertFalse(results.contains(transferTransaction));
	}
}