package de.deadlocker8.budgetmaster.settings;

import de.deadlocker8.budgetmaster.Build;
import de.deadlocker8.budgetmaster.accounts.AccountService;
import de.deadlocker8.budgetmaster.authentication.User;
import de.deadlocker8.budgetmaster.authentication.UserRepository;
import de.deadlocker8.budgetmaster.categories.CategoryService;
import de.deadlocker8.budgetmaster.categories.CategoryType;
import de.deadlocker8.budgetmaster.controller.BaseController;
import de.deadlocker8.budgetmaster.database.Database;
import de.deadlocker8.budgetmaster.database.DatabaseParser;
import de.deadlocker8.budgetmaster.database.DatabaseService;
import de.deadlocker8.budgetmaster.database.accountmatches.AccountMatchList;
import de.deadlocker8.budgetmaster.services.ImportService;
import de.deadlocker8.budgetmaster.update.BudgetMasterUpdateService;
import de.deadlocker8.budgetmaster.utils.LanguageType;
import de.deadlocker8.budgetmaster.utils.Strings;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.RandomUtils;
import de.thecodelabs.versionizer.UpdateItem;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


@Controller
public class SettingsController extends BaseController
{
	private final SettingsRepository settingsRepository;
	private final UserRepository userRepository;
	private final DatabaseService databaseService;
	private final AccountService accountService;
	private final CategoryService categoryService;
	private final ImportService importService;
	private final BudgetMasterUpdateService budgetMasterUpdateService;
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private final List<Integer> SEARCH_RESULTS_PER_PAGE_OPTIONS = Arrays.asList(10, 20, 25, 30, 50, 100);

	@Autowired
	public SettingsController(SettingsRepository settingsRepository, UserRepository userRepository, DatabaseService databaseService, AccountService accountService, CategoryService categoryService, ImportService importService, BudgetMasterUpdateService budgetMasterUpdateService)
	{
		this.settingsRepository = settingsRepository;
		this.userRepository = userRepository;
		this.databaseService = databaseService;
		this.accountService = accountService;
		this.categoryService = categoryService;
		this.importService = importService;
		this.budgetMasterUpdateService = budgetMasterUpdateService;
	}

	@RequestMapping("/settings")
	public String settings(WebRequest request, Model model)
	{
		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		request.removeAttribute("database", WebRequest.SCOPE_SESSION);
		return "settings/settings";
	}

	@PostMapping(value = "/settings/save")
	public String post(Model model, @ModelAttribute("Settings") Settings settings, BindingResult bindingResult,
					   @RequestParam(value = "password") String password,
					   @RequestParam(value = "passwordConfirmation") String passwordConfirmation,
					   @RequestParam(value = "languageType") String languageType)
	{
		settings.setLanguage(LanguageType.fromName(languageType));

		FieldError error = validatePassword(password, passwordConfirmation);
		if(error != null)
		{
			bindingResult.addError(error);
		}

		if(settings.getBackupReminderActivated() == null)
		{
			settings.setBackupReminderActivated(false);
		}

		if(bindingResult.hasErrors())
		{
			model.addAttribute("error", bindingResult);
			model.addAttribute("settings", settings);
			model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
			return "settings/settings";
		}
		else
		{
			if(!password.equals("•••••"))
			{
				BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
				String encryptedPassword = bCryptPasswordEncoder.encode(password);
				User user = userRepository.findByName("Default");
				user.setPassword(encryptedPassword);
				userRepository.save(user);
			}

			settingsRepository.delete(0);
			settingsRepository.save(settings);

			Localization.load();
		}

		return "redirect:/settings";
	}

	private FieldError validatePassword(String password, String passwordConfirmation)
	{
		if(password == null || password.equals(""))
		{
			return new FieldError("Settings", "password", password, false, new String[]{Strings.WARNING_SETTINGS_PASSWORD_EMPTY}, null, Strings.WARNING_SETTINGS_PASSWORD_EMPTY);
		}
		else if(password.length() < 3)
		{
			return new FieldError("Settings", "password", password, false, new String[]{Strings.WARNING_SETTINGS_PASSWORD_LENGTH}, null, Strings.WARNING_SETTINGS_PASSWORD_LENGTH);
		}

		if(passwordConfirmation == null || passwordConfirmation.equals(""))
		{
			return new FieldError("Settings", "passwordConfirmation", passwordConfirmation, false, new String[]{Strings.WARNING_SETTINGS_PASSWORD_CONFIRMATION_EMPTY}, null, Strings.WARNING_SETTINGS_PASSWORD_CONFIRMATION_EMPTY);
		}

		if(!password.equals(passwordConfirmation))
		{
			return new FieldError("Settings", "passwordConfirmation", passwordConfirmation, false, new String[]{Strings.WARNING_SETTINGS_PASSWORD_CONFIRMATION_WRONG}, null, Strings.WARNING_SETTINGS_PASSWORD_CONFIRMATION_WRONG);
		}

		return null;
	}

	@RequestMapping("/settings/database/requestExport")
	public void downloadFile(HttpServletResponse response)
	{
		LOGGER.debug("Exporting database...");
		String data = databaseService.getDatabaseAsJSON();
		try
		{
			byte[] dataBytes = data.getBytes("UTF8");
			String fileName = "BudgetMasterDatabase_" + DateTime.now().toString("yyyy_MM_dd") + ".json";
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			response.setContentType("application/json; charset=UTF-8");
			response.setContentLength(dataBytes.length);
			response.setCharacterEncoding("UTF-8");

			try(ServletOutputStream out = response.getOutputStream())
			{
				out.write(dataBytes);
				out.flush();
				LOGGER.debug("Exporting database DONE");
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	@RequestMapping("/settings/database/requestDelete")
	public String requestDeleteDatabase(Model model)
	{
		String verificationCode = RandomUtils.generateRandomString(RandomUtils.RandomType.BASE_58, 4, RandomUtils.RandomStringPolicy.UPPER, RandomUtils.RandomStringPolicy.DIGIT);
		model.addAttribute("deleteDatabase", true);
		model.addAttribute("verificationCode", verificationCode);
		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		return "settings/settings";
	}

	@PostMapping(value = "/settings/database/delete")
	public String deleteDatabase(Model model, @RequestParam("verificationCode") String verificationCode,
								 @RequestParam("verificationUserInput") String verificationUserInput)
	{
		if(verificationUserInput.equals(verificationCode))
		{
			LOGGER.info("Deleting database...");
			databaseService.reset();
			LOGGER.info("Deleting database DONE.");
		}
		else
		{
			return "redirect:/settings/database/requestDelete";
		}

		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		return "settings/settings";
	}

	@RequestMapping("/settings/database/requestImport")
	public String requestImportDatabase(Model model)
	{
		model.addAttribute("importDatabase", true);
		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		return "settings/settings";
	}

	@RequestMapping("/settings/database/upload")
	public String upload(WebRequest request, Model model, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
	{
		if(file.isEmpty())
		{
			return "redirect:/settings/database/requestImport";
		}

		try
		{
			String jsonString = new String(file.getBytes(), Charset.forName("UTF-8"));
			DatabaseParser importer = new DatabaseParser(jsonString, categoryService.getRepository().findByType(CategoryType.NONE));
			Database database = importer.parseDatabaseFromJSON();

			request.setAttribute("database", database, WebRequest.SCOPE_SESSION);
			return "redirect:/settings/database/accountMatcher";
		}
		catch(Exception e)
		{
			e.printStackTrace();

			model.addAttribute("errorImportDatabase", e.getMessage());
			model.addAttribute("settings", settingsRepository.findOne(0));
			model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
			return "settings/settings";
		}
	}

	@RequestMapping("/settings/database/accountMatcher")
	public String openAccountMatcher(WebRequest request, Model model)
	{
		model.addAttribute("database", request.getAttribute("database", WebRequest.SCOPE_SESSION));
		model.addAttribute("availableAccounts", accountService.getAllAccountsAsc());
		model.addAttribute("settings", settingsRepository.findOne(0));
		return "settings/import";
	}

	@RequestMapping("/settings/database/import")
	public String importDatabase(WebRequest request, @ModelAttribute("Import") AccountMatchList accountMatchList, Model model)
	{
		importService.importDatabase((Database) request.getAttribute("database", WebRequest.SCOPE_SESSION), accountMatchList);
		request.removeAttribute("database", RequestAttributes.SCOPE_SESSION);
		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		return "settings/settings";
	}

	@RequestMapping("/updateSearch")
	public String updateSearch()
	{
		budgetMasterUpdateService.getUpdateService().fetchCurrentVersion();
		return "redirect:/settings";
	}

	@RequestMapping("/update")
	public String update(Model model)
	{
		model.addAttribute("performUpdate", true);
		model.addAttribute("updateString", Localization.getString("info.text.update", Build.getInstance().getVersionName(), budgetMasterUpdateService.getAvailableVersionString()));
		model.addAttribute("settings", settingsRepository.findOne(0));
		model.addAttribute("searchResultsPerPageOptions", SEARCH_RESULTS_PER_PAGE_OPTIONS);
		return "settings/settings";
	}

	@RequestMapping("/performUpdate")
	public String performUpdate()
	{
		if(budgetMasterUpdateService.isRunningFromSource())
		{
			LOGGER.debug("Running from source code: Skipping update check");
			return "redirect:/settings";
		}

		UpdateItem.Entry entry = new UpdateItem.Entry(budgetMasterUpdateService.getAvailableVersion());
		try
		{
			budgetMasterUpdateService.getUpdateService().runVersionizerInstance(entry);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		LOGGER.info("Stopping BudgetMaster for update to version " + budgetMasterUpdateService.getAvailableVersionString());
		System.exit(0);

		return "";
	}
}