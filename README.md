# Informatics Large Practical -- Autonoumous Drone Route Design

The first part is to map colors to the correspondging confinement areas.

```bash
java -jar heatmap-0.0.1-SNAPSHOT.jar ../predictions.txt
```

The second part is to design a graph algorithm, which helps to design a route for autonomous drone to pass through 33 points in the confinement area. Each step is 0.0003. A legal record should within a 0.0002 degree from the nearest point. The total step should less than 150. 
