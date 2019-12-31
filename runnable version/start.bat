start cmd /k "java -jar v0.1.jar 30300"

for /l %%x in (1, 1, 10) do (
  start cmd /k "java -jar v0.1.jar localhost 30300"
)
