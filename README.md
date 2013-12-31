JWO Refinement Tools
====================

Japanese Wikipedia Ontology (JWO) Refinement Tools

## Requirements
* [Eclipse 4.2](http://www.eclipse.org/juno/)
* [sbt 0.13.1](http://www.scala-sbt.org)
* [Scala IDE for Eclipse](http://scala-ide.org/download/current.html)
 * [For Scala 2.9.x](http://download.scala-ide.org/sdk/e38/scala29/stable/site)
* [DODDLE-OWL](http://doddle-owl.sourceforge.net/en/)

## Acknowledgment
* [Japanese WordNet](http://nlpwww.nict.go.jp/wn-ja/index.en.html)
* [Japanese Wikipedia Ontology](http://www.wikipediaontology.org/)
* [Apache Jena](http://jena.apache.org)
* [ScalaQuery](http://scalaquery.org)
* [sqlite-jdbc] (https://bitbucket.org/xerial/sqlite-jdbc)
* [Swing for Scala] (http://mvnrepository.com/artifact/org.scala-lang/scala-swing/2.9.3)

## Download refined JWO
You can download refined JWO as follows. 

* [refined_jwo_20131225.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/refined_jwo_20131225.owl)
* [refined_jwo_class_instance_20131225.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/refined_jwo_class_instance_20131225.owl)

## Publications
* Takeshi Morita, Yuka Sekimoto, Susumu Tamagawa, Takahira Yamaguchi, "Building up a Class Hierarchy with Properties from Japanese Wikipedia", 2012 IEEE/WIC/ACM International Conference on Web Intelligence, pp. 514-521 (2012) ([PDF](http://dl.acm.org/citation.cfm?id=2457639))
* 森田 武史, 玉川 奨, 山口 高平, "オントロジーアライメントを用いた日本語Wikipediaオントロジーと日本語WordNetの統合", 第28回 セマンティックウェブとオントロジー研究会 SIG-SWO-A1202-07, (2012) ([PDF](http://sigswo.org/papers/SIG-SWO-A1202/SIG-SWO-A1202-07.pdf))
* 森田 武史, 関本 有佳, 玉川 奨, 山口 高平, "日本語Wikipediaからのプロパティ付きクラス階層の構築と評価", 人工知能学会 セマンティックWebとオントロジー研究会 第26回 SIG-SWO-A1103-06, (2011) ([PDF](http://sigswo.org/papers/SIG-SWO-A1103/SIG-SWO-A1103-06.pdf))

## Procedures used to refine JWO

1. [Extracting class-instance relationships](#p1)
2. [Refining class-instance relationships and identifying alignment target classes](#p2)
3. [Aligning JWO classes and JWN synsets](#p3)
4. [Integrating JWO and JWN using DODDLE-OWL](#p4)
5. [Removing redundant class-instance relationships](#p5)
6. [Defining the domains of properties based on a consideration of property inheritance](#p6)
7. [Buiding Refined JWO](#p7)

### <a name="p1"> 1. Extracting class-instance relationships

#### 1-1. [class_instance_extractor.ClassInstanceExtractor.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/ClassInstanceExtractor.scala)
* Input  
 * [ontologies/wikipediaontology_instance_20101114ja.rdf](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/ontologies/wikipediaontology_instance_20101114ja.rdf)
* Outputs
 * [inputs_and_outputs/class-instance.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance.txt)
 * [inputs_and_outputs/class-instance-cnt.csv](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance-cnt.csv)

#### 1-2. [class_instance_extractor.ClassInstanceExtractorFromRole.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/ClassInstanceExtractorFromRole.scala)
* Input
 * [ontologies/wikipediaontology_instance_20101114ja.rdf](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/wikipediaontology_instance_20101114ja.rdf)
* Oputput
 * [inputs_and_outputs/class-instance_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance_from_role.txt)

#### 1-3. [class_instance_extractor.ConvertClassInstanceListToSQLiteDB.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/ConvertClassInstanceListToSQLiteDB.scala)
* Inputs
 * [inputs_and_outputs/class-instance.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance.txt)
 * [inputs_and_outputs/class-instance_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance_from_role.txt)
* Oputputs
 * [inputs_and_outputs/class_instance_list_from_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_type.db)
 * [inputs_and_outputs/class_instance_list_from_role.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_role.db)

#### 1-4. [class_instance_extractor.ClassInstanceExtractorFromDB.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/ClassInstanceExtractorFromDB.scala)
* Inputs
 * [inputs_and_outputs/class_instance_list_from_role.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_role.db)
 * [inputs_and_outputs/class_instance_list_from_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_type.db)
* Outputs
 * [inputs_and_outputs/merged_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged_class_instance_list.db)
 * [inputs_and_outputs/duplicated_class-instance_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/duplicated_class-instance_from_role.txt)

### <a name="p2"> 2. Refining class-instance relationships and identifying alignment target classes
#### 2-1. [class_extractor.ClassExtractorFromSqliteDB.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_extractor/ClassExtractorFromSqliteDB.scala)
* Inputs
 * [inputs_and_outputs/merged_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged_class_instance_list.db)
 * [inputs_and_outputs/class_instance_list_from_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_type.db)
 * [inputs_and_outputs/class_instance_list_from_role.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_role.db)
* Outputs 
 * [inputs_and_outputs/merged-class-list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged-class-list.txt)
 * [inputs_and_outputs/class-list_from_type.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_type.txt)
 * [inputs_and_outputs/class-list_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_role.txt)

#### 2-2. [class_instance_refinement_tool.ClassInstanceRefinementTool.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_refinement_tool/ClassInstanceRefinementTool.scala)
* Inputs
 * [inputs_and_outputs/merged_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged_class_instance_list.db)
 * [inputs_and_outputs/merged-class-list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged-class-list.txt)
* Output
 * [class-instance-refinement-results-20120302.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/class-instance-refinement-results-20120302.txt)

#### 2-3. [class_extractor.RefinedClassExtractor.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_extractor/RefinedClassExtractor.scala)
* Inputs
 * [inputs_and_outputs/class-list_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_role.txt)
 * [inputs_and_outputs/class-list_from_type.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_type.txt)
 * [inputs_and_outputs/class-instance-refinement-results-20120302.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance-refinement-results-20120302.txt)
* Outputs
 * [inputs_and_outputs/refined_class_list_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_list_from_role.txt)
 * [inputs_and_outputs/refined_class_list_from_type.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_list_from_type.txt)
 * [inputs_and_outputs/refined_class_list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_list.txt)

#### 2-4. [class_instance_extractor.RefinedClassInstanceExtractor.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/RefinedClassInstanceExtractor.scala)
* Inputs
 * [inputs_and_outputs/class-list_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_role.txt)
 * [inputs_and_outputs/class-list_from_type.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_type.txt)
 * [inputs_and_outputs/class-instance-refinement-results-20120302.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance-refinement-results-20120302.txt)
 * [inputs_and_outputs/merged_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged_class_instance_list.db)
* Output
 * [inputs_and_outputs/refined_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list.db)

#### 2-5. [class_instance_refinement_tool.DBDuplicationCheck.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_refinement_tool/DBDuplicationCheck.scala)
* Input
 * [inputs_and_outputs/refined_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list.db)
* Output
 * [inputs_and_outputs/refined_class_instance_list2.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list2.db)

#### 2-6. [class_instance_refinement_tool.AnalyzeClassInstanceExperiments.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_refinement_tool/AnalyzeClassInstanceExperiments.scala)
* Inputs
 * [inputs_and_outputs/class-list_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_role.txt)
 * [inputs_and_outputs/class-list_from_type.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-list_from_type.txt)
 * [inputs_and_outputs/class-instance-refinement-results-20120302.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance-refinement-results-20120302.txt)
* Output
 * [inputs_and_outputs/alignment-target-class-list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/alignment-target-class-list.txt)

### <a name="p3"> 3. Aligning JWO classes and JWN synsets
#### 3-1. [jwo_jwn_alignment_tool.SynonymExtractorFromJpWn.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/jwo_jwn_alignment_tool/SynonymExtractorFromJpWn.scala)
* Inputs
 * [ontologies/JPNWN1.1.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/JPNWN1.1.owl)
 * [inputs_and_outputs/extract_synonyms_from_jpwn.sparql](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/extract_synonyms_from_jpwn.sparql)
* Outputs
 * [inputs_and_outputs/jpwn1.1_synonyms_ja.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/jpwn1.1_synonyms_ja.txt)

#### 3-2. [jwo_jwn_alignment_tool.JWOandJWNAlignment.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/jwo_jwn_alignment_tool/JWOandJWNAlignment.scala)
* Inputs
 * [inputs_and_outputs/alignment-target-class-list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/alignment-target-class-list.txt)
 * [inputs_and_outputs/jpwn1.1_synonyms_ja.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/jpwn1.1_synonyms_ja.txt)
* Output
 * [inputs_and_outputs/calculating_jwo_jwn_similarity_results.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/calculating_jwo_jwn_similarity_results.txt)

#### 3-3. [ontology_builder.ConvertJWNOWLToTDB.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/ontology_builder/ConvertJWNOWLToTDB.scala)
* Inputs
 * [ontologies/JPNWN1.1.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/JPNWN1.1.owl)
 * [ontologies/JPNWN1.1_tree.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/JPNWN1.1_tree.owl)
* Output
 * [ontologies/jwn1.1_tdb](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/jwn1.1_tdb)

#### 3-4. [jwo_jwn_alignment_tool.JWOandJWNAlignmentTool.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/jwo_jwn_alignment_tool/JWOandJWNAlignmentTool.scala)
* Inputs
 * [inputs_and_outputs/alignment-target-class-list.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/alignment-target-class-list.txt)
 * [inputs_and_outputs/jpwn1.1_synonyms_ja.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/jpwn1.1_synonyms_ja.txt)
 * [inputs_and_outputs/merged_class_instance_list.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/merged_class_instance_list.db)
 * [ontologies/jwn1.1_tdb](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/jwn1.1_tdb)
* Outputs
 * [inputs_and_outputs/alignment_results.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/alignment_results.db)
 * [inputs_for_DODDLE/inputWordList.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_for_DODDLE/inputWordList.txt)
 * [inputs_for_DODDLE/inputWordConceptList.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_for_DODDLE/inputWordConceptList.txt)
 * [inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt)

### <a name="p4">4. Integrating JWO and JWN using DODDLE-OWL
#### 4-1. [DODDLE-OWL](http://doddle-owl.sourceforge.net/en/)
* Inputs
 * [inputs_for_DODDLE/inputWordList.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_for_DODDLE/inputWordList.txt)
 * [inputs_for_DODDLE/inputWordConceptList.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_for_DODDLE/inputWordConceptList.txt)
* Output
 * [ontologies/ontology_constructed_by_doddle.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/ontology_constructed_by_doddle.owl)

#### 4-2. [ontology_builder.OntologyBuilder.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/ontology_builder/OntologyBuilder.scala)
* Inputs
 * [ontologies/ontology_constructed_by_doddle.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/ontology_constructed_by_doddle.owl)
 * [inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt)
 * [inputs_and_outputs/class-instance-refinement-results-20120302.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class-instance-refinement-results-20120302.txt)
* Output
 * [ontologies/merged_ontology_20120316.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/merged_ontology_20120316.owl)

### <a name="p5">5. Removing redundant class-instance relationships
#### 5-1. [class_instance_refinement_tool.RemoveRedundantClassInstance.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_refinement_tool/RemoveRedundantClassInstance.scala)
* Inputs
 * [ontologies/merged_ontology_revised_by_hand_20130912.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/merged_ontology_revised_by_hand_20130912.owl)
 * [inputs_and_outputs/refined_class_instance_list2.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list2.db)
* Outputs
 * [inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db)
 * [inputs_and_outputs/redundant_type_set.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/redundant_type_set.txt)

### <a name="p6">6. Defining the domains of properties based on a consideration of property inheritance
#### 6-1. [class_property_extractor.ClassPropertyExtractor.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_property_extractor/ClassPropertyExtractor.scala)
* Inputs
 * [inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db)
 * [ontologies/wikipediaontology_instance_20101114ja.rdf](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/wikipediaontology_instance_20101114ja.rdf)
* Output
 * [inputs_and_outputs/class_property_list_with_count.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_property_list_with_count.db)

#### 6-2. [property_elevator.PropertyElevator.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/property_elevator/PropertyElevator.scala)
* Inputs
 * [ontologies/merged_ontology_20130912.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/merged_ontology_20130912.owl)
 * [inputs_and_outputs/class_property_list_with_count.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_property_list_with_count.db)
* Outputs
 * [inputs_and_outputs/class_elevated_property_list_with_label_and_depth.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_elevated_property_list_with_label_and_depth.db)
 * [inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db)

### <a name="p7">7. Buiding Refined JWO
#### 7-1. [ontology_builder.RefinedJWOBuilder.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/ontology_builder/RefinedJWOBuilder.scala)
* Inputs
 * [inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db)
 * [inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db)
 * [ontologies/merged_ontology_revised_by_hand_20130912.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/merged_ontology_revised_by_hand_20130912.owl)
* Outputs
 * [ontologies/refined_jwo_class_instance_20131225.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/refined_jwo_class_instance_20131225.owl)
 * [ontologies/refined_jwo_20131225.owl](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/refined_jwo_20131225.owl)

## Test Programs
#### [class_instance_extractor.RoleStatementsExtractor.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/RoleStatementsExtractor.scala)
* Inputs
 * [ontologies/wikipediaontology_instance_20101114ja.rdf](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/wikipediaontology_instance_20101114ja.rdf)
 * [ontologies/wikipediaontology_class_20101114ja.rdf](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/ontologies/wikipediaontology_class_20101114ja.rdf)
* Ouput
 * [inputs_and_outputs/tests/role_statements.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/tests/role_statements.txt)

#### [class_instance_extractor.ClassInstanceExtractorFromRoleStatements.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/ClassInstanceExtractorFromRoleStatements.scala)
* Input
 * [inputs_and_outputs/tests/role_statements.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/tests/role_statements.txt)
* Outputs
 * [input_and_outputs/tests/role-property-class.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/input_and_outputs/tests/role-property-class.txt)
 * [input_and_outputs/tests/role-property-class-cnt.csv](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/input_and_outputs/tests/role-property-class-cnt.csv)
 * [input_and_outputs/tests/class-instance_from_role_statements.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/input_and_outputs/tests/class-instance_from_role_statements.txt)

#### [class_instance_extractor.AnalyzeRoleStatements.scala](https://github.com/t-morita/JWO_Refinement_Tools/blob/master/src/main/scala/class_instance_extractor/AnalyzeRoleStatement.scala)
* Inputs
 * [inputs_and_outputs/duplicated_class-instance_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/duplicated_class-instance_from_role.txt)
 * [inputs_and_outputs/class_instance_list_from_role.db](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/class_instance_list_from_role.db)
* Output
 * [inputs_and_outputs/tests/added_class-instance_from_role.txt](https://github.com/t-morita/JWO_Refinement_Tools/tree/master/inputs_and_outputs/tests/added_class-instance_from_role.txt)

## Contact
* Author: Takeshi Morita
* E-mail: t_morita at si.aoyama.ac.jp

