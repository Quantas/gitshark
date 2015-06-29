package com.quantasnet.gitserver.git.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.model.RefHolder;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class RefController {

	private enum RefType {
		BRANCH("branch", "Branches", Constants.REFS_HEADS) {
			@Override
			public RevCommit getCommit(final Ref ref, final Repository db) {
				try (final RevWalk revWalk = new RevWalk(db)) {
					return revWalk.parseCommit(ref.getObjectId());
				} catch (final Exception e) {
				}
				return null;
			}
		},
		TAG("tag", "Tags", Constants.REFS_TAGS) {
			@Override
			public RevCommit getCommit(final Ref ref, final Repository db) {
				try (final RevWalk revWalk = new RevWalk(db)) {
					final Ref peeled = db.peel(ref);
				    return revWalk.parseCommit(peeled.getObjectId());
				} catch (final Exception e) {
				}
				return null;
			}
		};
		
		private static final RefType[] VALUES = values();
		
		final String name;
		final String title;
		final String refs;
		
		private RefType(final String name, final String title, final String refs) {
			this.name = name;
			this.title = title;
			this.refs = refs;
		}
		
		public abstract RevCommit getCommit(Ref ref, Repository db);
		
		public static RefType getForName(final String name) {
			for (final RefType type : VALUES) {
				if (type.name.equals(name)) {
					return type;
				}
			}
			return null;
		}
	}
	
	@RequestMapping(value = "/{refType:(?:branch|tag)}", method = RequestMethod.GET)
	public String branches(final GitRepository repo, @PathVariable final String refType, final Model model) throws Exception {
		
		final RefType type = RefType.getForName(refType);
		
		repo.execute(db -> {
			
			final List<RefHolder> refs = new ArrayList<>();
			
			final Map<String, Ref> branchRefs = db.getRefDatabase().getRefs(type.refs);
			for (final Map.Entry<String, Ref> entry : branchRefs.entrySet()) {
				refs.add(new RefHolder(type.getCommit(entry.getValue(), db), repo, entry.getKey()));
			}
			
			model.addAttribute("refType", type.name);
			model.addAttribute("refTitle", StringUtils.capitalize(type.name));
			model.addAttribute("sectionTitle", type.title);
			model.addAttribute("refs", refs);
		});
		
		return "git/refs";
	}
}
