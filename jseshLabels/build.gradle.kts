description = """
    Generic i18n texts, usable by all JSesh components.
        
    The final idea, even if it's not very modular, will be to put all 
    labels here, in order to ease editing.
    
    Also, the current libraries will use those files, but in a first step,
    without breaking them, even if we use at that time two or three different 
    systems.
""".trimIndent()

plugins {
    id("jsesh.java-conventions")
}
