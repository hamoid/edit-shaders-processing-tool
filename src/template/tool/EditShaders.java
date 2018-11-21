/**
 * Processing Shader Tool - simplify shader editing in Processing
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
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.tools.Tool;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

// when creating a tool, the name of the main class which implements Tool must
// be the same as the value defined for project.name in your build.properties

public class EditShaders implements Tool {
    private static final String PREF_KEY_EDITOR = "tools.shadertool.editor";
    // copied from
    // https://github.com/processing/processing/blob/master/app/src/processing/app/tools/ColorSelector.java
    private static volatile ShaderChooser shaderChooser;
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
                    shaderChooser = new ShaderChooser(base,
                            false, e -> {
                                //Clipboard c = Toolkit.getSystemClipboard();
                                //c.setContents(new StringSelection
                                // (shaderChooser.getHexColor()), null);
                            });
                    shaderChooser.setTemplatesPath(getShadersPath());

                    String editor = Preferences.get(PREF_KEY_EDITOR);
                    if(editor == null) {
                        if(Platform.isWindows()) {
                            editor = "notepad.exe,%PATH%/%FILE%";
                        } else if(Platform.isMacOS()) {
                            editor = "open,-e,%PATH%/%FILE%";
                        } else if(Platform.isLinux()) {
                            editor = "xterm,-e,cd %PATH% ; vi %FILE%";
                        }
                        Preferences.set(PREF_KEY_EDITOR, editor);
                    }
                    shaderChooser.setEditorCommand(editor);
                }
            }
        }
        shaderChooser.show();
        shaderChooser.populate();

        //base.getActiveEditor().statusNotice("data folder not found");
        // editor.setText("Deleted your code. What now?");
    }

    private File getShadersPath() {
        File toolPath = null;
        try {
            Path v = Paths.get(EditShaders.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI())
                    .getParent().getParent();
            toolPath = v.toFile();
        } catch (URISyntaxException e) {
        }
        if (toolPath != null) {
            return new File(toolPath + File.separator + "shaders");
        }
        return null;
    }
}
