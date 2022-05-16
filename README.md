# SlotMachine Excel I/O

This tool helps to convert given Excel files into correct JSON files for further usage. Various tasks can be executed with the command line. Excel I/O is designed to work with the Heuristic [Optimizer](https://github.com/jku-win-dke/mt2106-slma-optimizer). Additionally, Excel I/O provides a command to generate data for testing the Heuristic optimizer based on given parameters. Parameters include flight count, slot count, start time for slots, length per slot, length of margin windows, distribution setting of flights in the available time window, priority settings, different values regarding the calculated weights and the maximal time for calculating the optimum.

## Usage

Use `mvn clean package` to package the code.

Possible commands include:

- `-h` shows the help and available commands.
- `-cmwetj flights.xlsx optSession.json` generates a JSON file (in the correct format that the Heuristic Optimizer expects) from the source excel file and creates a excel file with the weight maps. `flights.xlsx` is the source excel file. `optSession.json` is the JSON file, which will be created (the tool includes the optimization identification in the file name).
- `-cmtj flights.xlsx optSession.json` generates a JSON file (in the correct format that the Heuristics Optimizer expects) from the source excel file and creates no excel file with weight maps. `flights.xlsx` is the source excel file. `optSession.json` is the JSON file, which will be created (the tool includes the optimization identification in the file name).
- `-wr flights.xlsx flightSequenceResult.json flights-e48aad2f-a746-4787-9f20-493a2286d900.xlsx` writes the results in the target excel file, which has been created with `-cmwetj` (or other commands). `flights.xlsx` is the source excel file (not necessary as a parameter and can be skipped). `flightSequenceResult.json` contains information about the optimized flight sequence order. `flights-e48aad2f-a746-4787-9f20-493a2286d900.xlsx` is the target excel file.
- `-tdg testNr001.json` generates test data based on the parameters stored in `testNr001.json`.
