module Data.Kore.ASTTraversalsTest where

import           Test.Tasty                 (TestTree, testGroup)
import           Test.Tasty.HUnit           (assertEqual, testCase)

import           Data.Kore.AST.Common
import           Data.Kore.AST.Kore
import           Data.Kore.AST.MetaOrObject
import           Data.Kore.ASTTraversals
import           Data.Kore.KoreHelpers

import           Control.Monad.Identity


lhs :: CommonKorePattern -> CommonKorePattern
lhs = patternTopDownVisitor leftImplies asKorePattern
  where
    leftImplies (ImpliesPattern ip) = Left (impliesFirst ip)
    leftImplies p                   = Right p

lhs2 :: CommonKorePattern -> CommonKorePattern
lhs2 = runIdentity . patternBottomUpVisitorM leftImplies
  where
    leftImplies (ImpliesPattern ip) = return (impliesFirst ip)
    leftImplies p                   = return (asKorePattern p)

astTraversalsTests :: TestTree
astTraversalsTests =
    testGroup
        "ASTTraversal Tests"
        [ testCase "Testing topDownVisitor"
            (assertEqual ""
                samplePatternExpected
                (lhs samplePattern)
            )
        , testCase "Testing bottomUpVisitorM"
            (assertEqual ""
                samplePatternExpected
                (lhs2 samplePattern)
            )
        ]
  where
    samplePatternExpected =
        asKorePattern $ ApplicationPattern Application
            { applicationSymbolOrAlias = SymbolOrAlias
                { symbolOrAliasConstructor = testId "sigma"
                , symbolOrAliasParams = []
                } :: SymbolOrAlias Object
            , applicationChildren =
                [ asKorePattern $ StringLiteralPattern $ StringLiteral "left1"
                ,  asKorePattern $ StringLiteralPattern $ StringLiteral "left2"
                ]
            }
    samplePattern =
        asKorePattern $ ApplicationPattern Application
            { applicationSymbolOrAlias = SymbolOrAlias
                { symbolOrAliasConstructor = testId "sigma"
                , symbolOrAliasParams = []
                } :: SymbolOrAlias Object
            , applicationChildren =
                [ asKorePattern $ ImpliesPattern Implies
                    { impliesSort = SortVariableSort $ SortVariable $
                        testId "#a" :: Sort Meta
                    , impliesFirst = asKorePattern $ StringLiteralPattern
                        (StringLiteral "left1")
                    , impliesSecond = asKorePattern $ StringLiteralPattern
                        (StringLiteral "right1")
                    }
                ,  asKorePattern $ ImpliesPattern Implies
                    { impliesSort = SortVariableSort $ SortVariable $
                        testId "#b" :: Sort Meta
                    , impliesFirst = asKorePattern $ StringLiteralPattern
                        (StringLiteral "left2")
                    , impliesSecond = asKorePattern $ StringLiteralPattern
                        (StringLiteral "right2")
                    }
                ]
            }

