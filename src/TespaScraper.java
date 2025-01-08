import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class TeamData {
	String teamName;
	ArrayList<String> matchHist;
	ArrayList<String> url;

	public TeamData(String name, ArrayList<String> hist, ArrayList<String> http) {
		teamName = name;
		matchHist = hist;
		url = http;
	}

	public String toString() {
		return teamName;
	}

	public int compareTo(TeamData team) throws ClassCastException {
		if (!(team instanceof TeamData))
			throw new ClassCastException("TeamData object expected");
		return this.teamName.compareTo(team.teamName);
	}

}

public class TespaScraper {
	static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		try {
			// fetch the document over HTTP
			Document doc = Jsoup.connect("https://compete.tespa.org/tournament/98/registrants").get();

			// get the page title
			String title = doc.title();
			System.out.println("title: " + title);

			// get all links in page
			Elements links = doc.select("a[href]");
			ArrayList<TeamData> Teams = new ArrayList<TeamData>();
			for (Element link : links) {
				// get the value from the href attribute
				if (link.attr("href").contains("team/")) {
					ArrayList<String> a1 = new ArrayList<String>();
					ArrayList<String> a2 = new ArrayList<String>();
					Teams.add(new TeamData(link.text(), a1, a2));
				}
			}
			TeamData[] T = Teams.toArray(new TeamData[Teams.size()]);
			TeamData[] scratch = new TeamData[Teams.size()];
			mergesort(T, scratch, 0, Teams.size() - 1);
			System.out.println(Arrays.toString(T));
			Document doc2 = Jsoup.connect("https://compete.tespa.org/tournament/98/phase/1/group/1").get();
			Elements links2 = doc2.select("a[href]");
			for (Element link : links2) {
				if (link.attr("href").contains("match/") & !(link.text().equals("TBD (Based on results)"))) {
					int i = binarySearch(T, link.text());
					if (i >= 0) {
						T[i].url.add((link.attr("href")));
						Document matchlink = Jsoup.connect(link.attr("href")).get();
						Elements teams = matchlink.select("h2");
						Elements score = matchlink.select("span");
						T[i].matchHist
								.add("\nTeams: " + teams.get(1).text() + " vs " + teams.get(2).text() + "\nScore: "
										+ score.get(1).text() + " " + score.get(2).text() + " " + score.get(3).text());
						Elements table = matchlink.select("table");
						String match = "";
						for (Element row : table.select("tr")) {
							Elements tds = row.select("td");
							match = match + tds.text() + "\n";
						}
						T[i].matchHist.add(match);
					}
				}
			}

			// for (TeamData team : T) {
			// for (String u : team.url) {
			// Document matchlink = Jsoup.connect(u).get();
			// Elements teams = matchlink.select("h2");
			// Elements score = matchlink.select("span");
			// team.matchHist.add("\nTeams: " + teams.get(1).text() + " vs " +
			// teams.get(2).text() + "\nScore: "
			// + score.get(1).text() + " " + score.get(2).text() + " " +
			// score.get(3).text());
			// Elements table = matchlink.select("table");
			// String match = "";
			// for (Element row : table.select("tr")) {
			// Elements tds = row.select("td");
			// match = match + tds.text() + "\n";
			// }
			// team.matchHist.add(match);
			// }
			// }
			
			System.out.print("Enter a team to look up,quit to stop: ");
			String line = sc.nextLine();
			while (!line.equals("quit")) {
				int j = teamCheck(line, T);
				if (j < 0) {
					System.out.println("Team with specified name is not in tournament!");
					System.out.print("Enter a team to look up,quit to stop: ");
					line = sc.nextLine();
				} else {
					int k = T[j].matchHist.size();
					boolean flag = true;
					for (int i = 0; i < k; i++) {
						if (flag) {
							System.out.print("Round " + (i / 2 + 1));
						}
						System.out.println(T[j].matchHist.get(i));
						flag = !flag;
					}
					System.out.print("Enter a team to look up,quit to stop: ");
					line = sc.nextLine();
				}
			}
			sc.close();
			System.exit(0);
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	// checks if team with specified name is in tournament. if so, returns the
	// index of team in arrayList Teams
	private static int teamCheck(String s, TeamData[] Teams) {
		int size = Teams.length;
		for (int i = 0; i < size; i++) {
			if (Teams[i].teamName.equals(s)) {
				return i;
			}
		}
		return -1;
	}

	private static void mergesort(TeamData[] list, TeamData[] scratch, int low, int high) {
		if (low < high) {
			int mid = low + ((high - low) >> 1);
			mergesort(list, scratch, low, mid);
			mergesort(list, scratch, mid + 1, high);
			// merge two subarrays into scratch space
			// copy back from scratch to list
			for (int i = low; i <= high; i++) {
				scratch[i] = list[i];
			}
			int i = low;
			int j = mid + 1;
			int k = low;
			while (i <= mid && j <= high) {
				if (scratch[i].teamName.compareTo(scratch[j].teamName) <= 0) {
					list[k] = scratch[i];
					i++;
				} else {
					list[k] = scratch[j];
					j++;
				}
				k++;
			}
			while (i <= mid) {
				list[k] = scratch[i];
				k++;
				i++;
			}
		}
	}

	private static int binarySearch(TeamData[] arr, String target) {
		int lo = 0, hi = arr.length - 1;
		while (lo <= hi) {
			int mid = (lo + hi) / 2;
			int c = target.compareTo(arr[mid].teamName);
			if (c == 0) { // target equal to arr[mid]
				return mid;
			}
			if (c < 0) { // target less than arr[mid]
				hi = mid - 1; // go left
			} else {
				lo = mid + 1; // go right
			}
		}
		return -1;
	}
}