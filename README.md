# A simple tool to facilitate working with shaders in Processing

Early Alpha quality! Try only if you know what you are doing.

The reason I'm sharing it even if it's not yet ready or polished
is because I already find it useful.

The GUI will improve much (I hope) in the next commits.

## Changelog

* 0.0.1 First version. Lists default shaders from Processing, allows 
adding the ones you choose to your current project for further editing.
Launching an external aditor should work. The editor can be configured
in preferences.txt
* 0.0.2 Previously the list of shaders found in your sketch would not
update when swithing between sketches. Now it checks every 100ms to
see if the editor has changed and rebuilds the list if it has.
* 0.0.3 Easier to grasp GUI. Ask for confirmations when deleting.
Warn when overwriting. Allow copying in both directions between
the sketch and the shader template folder. Add clone option.

