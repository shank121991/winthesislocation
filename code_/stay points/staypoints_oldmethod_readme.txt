staypoints_oldmethod:

This program:

Read the new locations(user file) in an online  input mode
  1. Everytime the hour changes, 
                  A. Find staypoints for the last hour and assign staypointID
                  B. Cluster staypoints based on distance for last hour, form states and assign stateID
                  C. Calculate state hourly weights for last hour
                  D. Predict based on trained data(if available)
  2. Everytime the date changes,
                  A. Add the days data into the training data
  3. If the hour and the time has not been changed, add the data to current hour data

Steps:
1. Change the user in variable "user"
2. Change the month to be considered in variable "month"
3. Change the threshold distances and time variables for staypoints and states in variable "state_d_thrhld", "staypts_d_thrhld", "staypts_t_thrhld"
4. Change Geolife source path as in variable "src_path"
5. Change destination base path in varaible "base_path"

Run the program!