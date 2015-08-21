package com.quantasnet.gitshark.git.ui;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepo;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.user.User;

@RequestMapping("/repo")
@Controller
public class RepoManageController {

	private static final String REPO_FORM = "repoForm";

	@Autowired
	private GitSharkDfsService dfsService;
	
	@RequestMapping
	public String myRepos(@AuthenticationPrincipal final User user, final Model model) throws GitSharkException {
		model.addAttribute("repos", dfsService.getAllReposForUser(user));
		return "git/repos";
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String create(final Model model) {
		if (!model.containsAttribute(REPO_FORM)) {
			model.addAttribute(REPO_FORM, new NewRepoForm());
		}
		return "git/repocreate";
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String createRepo(@AuthenticationPrincipal final User user, @Valid final NewRepoForm repoForm, final BindingResult bindingResult, final RedirectAttributes redirectAttributes) throws GitSharkException {

		if (!bindingResult.hasErrors()) {
			for (final GitSharkDfsRepo repo : dfsService.getAllReposForUser(user)) {
				if (repo.getName().equals(repoForm.getRepoName())) {
					bindingResult.rejectValue("repoName", "reponame.exists", "Repository already exists");
					break;
				}
			}
		}

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + REPO_FORM, bindingResult);
			redirectAttributes.addFlashAttribute(REPO_FORM, repoForm);
			return "redirect:/repo/create";
		}

		dfsService.createRepo(repoForm.getRepoName(), user);
		
		redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Repository created successfully.");
		return "redirect:/repo/" + user.getUserName() + '/' + repoForm.getRepoName();
	}

	static class NewRepoForm {

		@Size(min = 1, max = 200, message = "Repository name must be shorter than 200 characters.")
		@Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Repository name must not contain special characters or whitespace.")
		private String repoName;

		public String getRepoName() {
			return repoName;
		}

		public void setRepoName(String repoName) {
			this.repoName = repoName;
		}
	}

}
