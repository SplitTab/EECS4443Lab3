# EECS4443 Lab3 
## To-do list Application
### Group Members & Contributions:
- Stefewn Johnson (activity_task_detail.java + MainActivity)
- Muhammmad Zamin (activity_task_detail.java + MainActivity)
- Daniel Chahine (Activity_add_edit_task.java + MainActivity)
- Yuriy Kotyashko (Activity_add_edit_task + MainActivity)

### Creation of this theme:
The creation utilized follows the MVVM (Model-View-ViewModel) architectural pattern. The architectural pattern has been used due to how easily it is to separate concerns between each layer. 

MainActivity is the view; it handles the UI and the event calls, calling onto the necessary classes. 

TaskDetailActivity is the Model, it handles the mutation of present views and adds on top of already present data.

AddEditTaskActivity is the ViewModel, it creates and passes the instance onto the caller, MainActivity.

The layout used is ConstraintLayout because of how easy it is to align every child on the screen at the same time, while keeping the instance responsive for the user, and fewer view levels are allowed for cheaper performance implementation.

### Limitations faced:
No limitations were present during the implementation of this lab.
