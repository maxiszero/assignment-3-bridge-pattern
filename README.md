# Assignment 3 - Bridge Pattern

Java 17 Shape-Renderer example for Software Design Patterns, Astana IT University.
Student: **Olzhabekov Ali**, group **SE-2515**.

Repository: [maxiszero/assignment-3-bridge-pattern](https://github.com/maxiszero/assignment-3-bridge-pattern).

`Circle` and `Square` keep their geometry while switching between `VectorRenderer`
(real SVG files) and `RasterRenderer` (real PNG files). No external libraries or
build-tool downloads are required.

## Run

Install a full **JDK 17 or newer** and make `java` and `javac` available on PATH.
Open a terminal in this project directory.

Windows PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 test
powershell -ExecutionPolicy Bypass -File .\build.ps1 demo
```

macOS / Linux / Git Bash:

```sh
sh build.sh test
sh build.sh demo
```

The scripts compile with `--release 17 -Xlint:all -Werror`. `compile` only builds;
`test` runs the dependency-free test suite; `demo` runs `bridge.Main`.
Expected test summary: **PASS: 14 tests**. The tests do not require `-ea`.

The demo creates `output/circle.svg`, `output/square.svg`, `output/circle.png`
and `output/square.png`. Open the SVGs in a browser and the PNGs in an image viewer.
Each image is 104 x 104 logical units/pixels, with a blue figure on white.
Repeated runs overwrite these four generated files. To choose another directory:

```sh
java -Djava.awt.headless=true -cp build/classes bridge.Main another-output
```

## Design

| Bridge role | Class |
| --- | --- |
| Abstraction | `Shape` owns a replaceable `Renderer` reference |
| Refined Abstractions | `Circle`, `Square` |
| Implementor | `Renderer` interface |
| Concrete Implementors | `VectorRenderer`, `RasterRenderer` |
| Client | `Main` constructs and reconnects the objects |

```java
Shape circle = new Circle(40, vector);
circle.draw();
circle.setRenderer(raster);
circle.draw(); // same object and radius, different implementation
```

The bridge is object composition: `Shape` has a `Renderer`. There is no inheritance
between shapes and renderers. Both shape objects can share a renderer. SVG and PNG
encoding stay inside the implementation side. `DrawingFiles` centralizes file
output, `Dimensions` enforces sizes from 1 to 1000, and `DrawingStyle` holds the
shared padding and color.

Adding a renderer requires implementing the two operations and choosing it in the
client; existing shapes stay unchanged. A new shape that needs a new primitive
also requires extending the renderer interface and both implementations. This is
an intentional trade-off of the small interface, explained in the report.

## Files

```text
src/main/java/bridge/  Pattern classes, renderers, helpers and Main
src/test/java/bridge/  BridgeTest with 14 automated checks
docs/                 Report, UML source and defense notes
build.ps1, build.sh    Compile, test and demonstration commands
build/                Generated classes (ignored by Git)
output/               Generated drawings (ignored by Git)
```

In IntelliJ IDEA, open the folder, select JDK 17+, mark `src/main/java` as Sources
Root and `src/test/java` as Test Sources Root, then run `bridge.Main` or
`bridge.BridgeTest`. Set the working directory to the project root.

## Verification

Tests cover geometry delegation and preservation, switching both shapes between
both real renderers, valid SVG attributes, PNG dimensions and pixels, invalid and
boundary dimensions, null inputs, write failures, and locale-independent SVG.
See `docs/verification.txt` for the captured build environment and results.
GitHub Actions also compiles, tests and runs the demo with Temurin JDK 17 on
Windows and Linux after pushes to `main` and on pull requests.
Both jobs passed in the [verified Java 17 run](https://github.com/maxiszero/assignment-3-bridge-pattern/actions/runs/37194641274).

The report is `docs/Assignment_3_Bridge_Pattern_Report.pdf`.
Russian defense preparation is in `docs/DEFENSE_RU.md`.
The editable UML source is `docs/bridge.puml`; the diagram is `docs/bridge-uml.svg`.

## References

- [Bridge explanation from the assignment reading list](https://refactoring.guru/design-patterns/bridge)
- [Java 17 Graphics2D API](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Graphics2D.html)
- [Java 17 ImageIO API](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/imageio/ImageIO.html)
