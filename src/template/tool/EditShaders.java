/**
 * you can put a one sentence description of your tool here.
 * <p>
 * ##copyright##
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
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

import processing.app.Base;
import processing.app.tools.Tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

// when creating a tool, the name of the main class which implements Tool must
// be the same as the value defined for project.name in your build.properties

public class EditShaders implements Tool {
    // copied from
    // https://github.com/processing/processing/blob/master/app/src/processing/app/tools/ColorSelector.java
    private static volatile ShaderChooser shaderChooser;
    private static String templatePath = "/home/funpro/src/P5/processing/core" +
            "/src/processing/opengl/shaders";
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
        if (shaderChooser == null) {
            synchronized (EditShaders.class) {
                if (shaderChooser == null) {
                    shaderChooser = new ShaderChooser(base.getActiveEditor(),
                            false, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //Clipboard c = Toolkit
                            // .getSystemClipboard();
                            //c.setContents(new StringSelection
                            // (shaderChooser.getHexColor()), null);
                        }
                    });
                }
            }
        }
        shaderChooser.show();
        shaderChooser.setPaths(
                new File(templatePath),
                base.getActiveEditor().getSketch().getDataFolder());
        shaderChooser.populate();

        //base.getActiveEditor().statusNotice("data folder not found");
        // editor.setText("Deleted your code. What now?");
        // System.out.println("Hello");
    }
}
