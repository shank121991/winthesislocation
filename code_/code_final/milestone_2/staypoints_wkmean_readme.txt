staypoints_wkmean:

This program:

1. Find staypoints
2. Group staypoints together(snapping to existing staypoint)
3. Apply kmean clustering on staypoints
4. Create hourly weights
5. Create transition matrices
6. Create markov chains

for a user for a duration of time.

Steps:

1. Change variable "user" to select user
2. Change Geolife file source path in variable "src_path"
3. Change destination folder path in variable "dest_path"
4. Change from and to date variables in "from_date" and "to_date" respectively to select the duration
5. Change variable flag "fltr_data_date_rng" to "YES" if this duration needs to be selected or "NO" if you want the entire user file to be processed.

Run the program!