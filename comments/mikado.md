# Attempts of graphs for Mikado Method


**Legend**

- ✅ : done
- ❓ : questionnable goal
- red line : root goal


## Explicit language choice

*Allow users to choose the language of JSesh interface*

```mermaid
graph TB
    classDef default fill:yellow,stroke:#333,stroke-width:2px;
    classDef root stroke-width:4px,stroke:red,border-radius:10pt;

    choice(Allow UI language choice):::root
    parameter(Store language choice in a parameter)
    control(Allow the user to change the language choice)

    choice --> parameter
    choice --> control
```
## Arabic I18n

```mermaid
graph TB
    classDef default fill:yellow,stroke:#333,stroke-width:2px;
    classDef root stroke-width:4px,stroke:red,border-radius:10pt;


    arabic(Arabic I18n):::root
    arabicText("✅Translate labels<br>to Arabic")
    fixTextOrder("❓fix text order<br>avoid cases where text is <br>produced implicitly")
    fixComponentOrder("Fix Components Order for R2L<br>see setComponentOrientation...")

    arabic --> arabicText
    arabic --> fixTextOrder
    arabic --> fixComponentOrder
```

Comments

- Arabic i18n is harder than we thought as both right to left text order must be enforced and  
- fixing text order might not be necessary if we fix window layout (but it might be useful for some languages)

## Rationalization of Layout preferences

**Goals**

- make preferences choice explicit
- make preferences immutable to avoid unwanted side effect