# spring-batch-poc
A simple CSV processor with spring batch


- The majority of the code is self-explanatory, but here are some key points:
    - The application utilizes PostgreSQL as its backend database server, and the corresponding configuration can be found in [src/main/resources/application.properties].
    - Currently, there are only two configurable fields in the database: the number of threads and the maximum number of retries. The settings keys for these configurations are `setting.batch.numberOfThreads` and `setting.batch.maxRetryCountSettingKey`, respectively, in the settings database table.
    - By default, the output files are stored in the application's source code directory. However, it is possible to change this setting in the `FileSystemService.java#BASE_WORKING_DIRECTORY`.

