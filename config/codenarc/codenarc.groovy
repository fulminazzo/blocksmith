ruleset {
    ruleset('rulesets/design.xml')
    ruleset('rulesets/groovyism.xml')
    ruleset('rulesets/unused.xml')

    ruleset('rulesets/basic.xml') {
        'EmptyClass' { enabled = false }
    }

    ruleset('rulesets/imports.xml') {
        'MisorderedStaticImports' { enabled = false }
        'NoWildcardImports' { enabled = false }
    }

    ruleset('rulesets/convention.xml') {
        // Disables requirement for @CompileStatic
        'CompileStatic' { enabled = false }
        // Enables use of 'def' keyword
        'NoDef' { enabled = false }
        'VariableTypeRequired' { enabled = false }
        'FieldTypeRequired' { enabled = false }
        'MethodReturnTypeRequired' { enabled = false }
        'ImplicitClosureParameter' { enabled = false }
        'TrailingComma' { enabled = false }
    }

    ruleset('rulesets/dry.xml') {
        'DuplicateStringLiteral' { enabled = false }
        'DuplicateNumberLiteral' { enabled = false }
        'DuplicateListLiteral' { enabled = false }
        'DuplicateMapLiteral' { enabled = false }
    }

    ruleset('rulesets/exceptions.xml') {
        'CatchException' { enabled = false }
    }

    ruleset('rulesets/formatting.xml') {
        'SpaceAroundOperator' { enabled = false }
        'ClassStartsWithBlankLine' { enabled = false }
        'SpaceAroundMapEntryColon' {
            characterBeforeColonRegex = /\s/
            characterAfterColonRegex = /\s/
        }
        // Increased lines size for Spock data tables
        'LineLength' { length = 240 }
    }

    ruleset('rulesets/unnecessary.xml') {
        'UnnecessaryReturnKeyword' { enabled = false }
        // Interferes with Spock data tables formatting
        'UnnecessaryBooleanExpression' { enabled = false }
    }

}