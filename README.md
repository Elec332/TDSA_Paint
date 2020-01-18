# TDSA_Paint

Requirements: Java 1.8+

Small how-to-use:

## Top menu bar
### File
The options in this menu are pretty self-explanatory. From here you can start a new project, open a previously saved project and save your current project. You can also close the program in this menu. 

### Layers
In this menu you can manage your paint layers (Like in paintDotNet)
You can set a layer to active by clicking it. All edit and draw actions will be performed on the layer that is currently active.
From here you can also open a mini-window to manage your layers. You can change the order by scrolling over them, set them to avtive by clicking on them. And you can enable/disable whether the layer should be drawn/shown by checking/unchecking the checkboxes.

### Options
From here you can enter edit mode or drawing mode. You can also start "server" or "client" mode, which allow you to respectively host or connect to remote layers.

#### Paint modes (Only shown when in "Draw" mode)
From here you can add a layer, set the current drawing color, or start drawing lines, squares, ovals or freeform curves.

#### Edit (Only shown when in "Edit" mode)
From here you can group and "ungroup" multiple objects. You can also delete objects from here.
When in edit mode, you can see a light-gray outline of the bounding boxes of the objects in the layer that is currently active. (This behaviour can be disabled in the "Edit" menu)

When an object is selected it will get a black outline with small circles on each corner. When "grouping", all selected objects will be grouped. When a object is "focussed" it will have a blue outline. Focussed objects can be dragged or resized (mirroring during a resize is supported). To unselect an object, simply click somewhere in the focussed object. 

When you want to change the color of an object, or if you want to fill e.g. a rectangle, you can use the right mouse click to activate a dialog with options to alter the color of an object. (You can also fill objects from here)

#### Client (Only shown when in "Network client" mode)
From here you can connect to/disconnect from remote layers.
When the connect button is clicked, a menu will pop up showing all shared layers in your LAN. Simply press "join" to join a layer.
You also can edit and draw in remote layers. (You cannot join the same layer twice)

#### Server (Only shown when in "Network server" mode)
From here you can start/stop sharing layers. You can also share remote layers.
(You cannot join layers you shared yourself)
