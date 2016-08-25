/**
 * you can put a one sentence description of your tool here.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author ##author##
 * @modified ##date##
 * @version ##tool.prettyVersion##
 */

package template.tool;

import java.io.File;
import java.io.FilenameFilter;

import processing.app.Base;
import processing.app.tools.Tool;

// when creating a tool, the name of the main class which implements Tool must
// be the same as the value defined for project.name in your build.properties

public class EditShaders implements Tool {
	Base base;

	@Override
	public String getMenuTitle() {
		return "##tool.name##";
	}

	@Override
	public void init(Base base) {
		// Store a reference to the Processing application itself
		this.base = base;
	}

	@Override
	public void run() {
		File dataFolder = base.getActiveEditor().getSketch().getDataFolder();

		if (dataFolder.exists() && dataFolder.isDirectory()) {

			String dataPath = dataFolder.getAbsolutePath();

			File[] files = dataFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".glsl");
				}
			});
			StringBuilder sb = new StringBuilder();
			for (File f : files) {
				sb.append(" -O " + f.getName());
			}

			Process p;
			try {
				p = Runtime.getRuntime().exec(new String[] { "xterm", "-hold",
						"-e", "cd " + dataPath + " ; vi" + sb });
				// p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// editor.setText("Deleted your code. What now?");
		// System.out.println("Hello");
	}
}
