JWO Refinement Tools
====================

Japanese Wikipedia Ontology (JWO) Refinement Tools

## Requirements
* [Eclipse 4.2](http://www.eclipse.org/juno/)
* [Scala IDE for Eclipse](http://scala-ide.org/download/current.html)
 * [For Scala 2.9.x](http://download.scala-ide.org/sdk/e38/scala29/stable/site)

## Download Refined JWO
You can download Refined JWO as follows. 

* [refined_jwo_20131225.owl]()
* [refined_jwo_class_instance_20131225.owl]()

## Procedures used to refine JWO

[1. Extracting class-instance relationships](#p1)

### <a name="p1"> 1. Extracting class-instance relationships

#### 1-1. class_instance_extractor.ClassInstanceExtractor.scala
* Input  
 * ontologies/wikipediaontology_instance_20101114ja.rdf
* Outputs
 * inputs_and_outputs/class-instance.txt
 * inputs_and_outputs/class-instance-cnt.csv

#### 1-2. class_instance_extractor.ClassInstanceExtractorFromRole.scala
* Input
 * ontologies/wikipediaontology_instance_20101114ja.rdf
* Oputput
 * inputs_and_outputs/class-instance_from_role.txt

#### 1-3. class_instance_extractor.ConvertClassInstanceListToSQLiteDB.scala
* Inputs
 * inputs_and_outputs/class-instance.txt
 * inputs_and_outputs/class-instance_from_role.txt
* Oputputs
 * inputs_and_outputs/class_instance_list_from_type.db
 * inputs_and_outputs/class_instance_list_from_role.db

#### 1-4. class_instance_extractor.ClassInstanceExtractorFromDB.scala
* Inputs
 * inputs_and_outputs/class_instance_list_from_role.db
 * inputs_and_outputs/class_instance_list_from_type.db
* Outputs
 * inputs_and_outputs/merged_class_instance_list.db
 * inputs_and_outputs/duplicated_class-instance_from_role.txt

### 2. Refining class-instance relationships and identifying alignment target classes
#### 2-1. class_extractor.ClassExtractorFromSqliteDB.scala
* Inputs
 * inputs_and_outputs/merged_class_instance_list.db
 * inputs_and_outputs/class_instance_list_from_type.db
 * inputs_and_outputs/class_instance_list_from_role.db
* Outputs 
 * inputs_and_outputs/merged-class-list.txt
 * inputs_and_outputs/class-list_from_type.txt 
 * inputs_and_outputs/class-list_from_role.txt

#### 2-2. class_instance_refinement_tool.ClassInstanceRefinementTool.scala
* Inputs
 * inputs_and_outputs/merged_class_instance_list.db
 * inputs_and_outputs/merged-class-list.txt
* Output
 * class-instance-refinement-results-20120302.txt

#### 2-3. class_extractor.RefinedClassExtractor.scala
* Inputs
 * inputs_and_outputs/class-list_from_role.txt
 * inputs_and_outputs/class-list_from_type.txt
 * inputs_and_outputs/class-instance-refinement-results-20120302.txt
* Outputs
 * inputs_and_outputs/refined_class_list_from_role.txt
 * inputs_and_outputs/refined_class_list_from_type.txt
 * inputs_and_outputs/refined_class_list.txt

#### 2-4. class_instance_extractor.RefinedClassInstanceExtractor.scala
* Inputs
 * inputs_and_outputs/class-list_from_role.txt
 * inputs_and_outputs/class-list_from_type.txt
 * inputs_and_outputs/class-instance-refinement-results-20120302.txt
 * inputs_and_outputs/merged_class_instance_list.db
* Output
 * inputs_and_outputs/refined_class_instance_list.db

#### 2-5. class_instance_extractor.DBDuplicationCheck.scala
* Input
 * inputs_and_outputs/refined_class_instance_list.db
* Output
 * inputs_and_outputs/refined_class_instance_list2.db

#### 2-6. class_instance_refinement_tool.AnalyzeClassInstanceExperiments.scala
* Inputs
 * inputs_and_outputs/class-list_from_role.txt
 * inputs_and_outputs/class-list_from_type.txt
 * inputs_and_outputs/class-instance-refinement-results-20120302.txt
* Output
 * inputs_and_outputs/alignment-target-class-list.txt

### 3. Aligning JWO classes and JWN synsets
#### 3-1. jwo_jwn_alignment_tool.SynonymExtractorFromJpWn.scala
* Inputs
 * ontologies/JPNWN1.1.owl
 * inputs_and_outputs/extract_synonyms_from_jpwn.sparql
* Outputs
 * inputs_and_outputs/jpwn1.1_synonyms_ja.txt

#### 3-2. jwo_jwn_alignment_tool.JWOandJWNAlignment.scala
* Inputs
 * inputs_and_outputs/alignment-target-class-list.txt
 * inputs_and_outputs/jpwn1.1_synonyms_ja.txt
* Output
 * inputs_and_outputs/calculating_jwo_jwn_similarity_results.txt

#### 3-3. ontology_builder.ConvertJWNOWLToTDB.scala
* Inputs
 * ontologies/JPNWN1.1.owl
 * ontologies/JPNWN1.1_tree.owl
* Output
 * ontologies/jwn1.1_tdb

#### 3-4. jwo_jwn_alignment_tool.JWOandJWNAlignmentTool.scala
* Inputs
 * inputs_and_outputs/alignment-target-class-list.txt
 * inputs_and_outputs/jpwn1.1_synonyms_ja.txt
 * inputs_and_outputs/merged_class_instance_list.db
 * ontologies/jwn1.1_tdb
* Outputs
 * inputs_and_outputs/alignment_results.db
 * inputs_for_DODDLE/inputWordList.txt
 * inputs_for_DODDLE/inputWordConceptList.txt
 * inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt

### 4. Integrating JWO and JWN using DODDLE-OWL
#### 4-1. DODDLE-OWL
* Inputs
 * inputs_for_DODDLE/inputWordList.txt
 * inputs_for_DODDLE/inputWordConceptList.txt
* Output
 * ontologies/ontology_constructed_by_doddle.owl

#### 4-2. ontology_builder.OntologyBuilder.scala
* Inputs
 * ontologies/ontology_constructed_by_doddle.owl
 * inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt
 * inputs_and_outputs/class-instance-refinement-results-20120302.txt
* Output
 * ontologies/merged_ontology_20120316.owl

### 5. Removing redundant class-instance relationships
#### 5-1. class_instance_refinement_tool.RemoveRedundantClassInstance.scala
* Inputs
 * ontologies/merged_ontology_revised_by_hand_20130912.owl
 * inputs_and_outputs/refined_class_instance_list2.db
* Outputs
 * inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * inputs_and_outputs/redundant_type_set.txt

### 6. Defining the domains of properties based on a consideration of property inheritance
#### 6-1. class_property_extractor.ClassPropertyExtractor.scala
* Inputs
 * inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * ontologies/wikipediaontology_instance_20101114ja.rdf
* Output
 * inputs_and_outputs/class_property_list_with_count.db

#### 6-2. property_elevator.PropertyElevator.scala
* Inputs
 * ontologies/merged_ontology_20130912.owl
 * inputs_and_outputs/class_property_list_with_count.db
* Outputs
 * inputs_and_outputs/class_elevated_property_list_with_label_and_depth.db
 * inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db

### 7. Buiding Refined JWO
#### 7-1. ontology_builder.RefinedJWOBuilder.scala
* Inputs
 * inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db
 * ontologies/merged_ontology_revised_by_hand_20130912.owl
* Outputs
 * ontologies/refined_jwo_class_instance_20131225.owl
 * ontologies/refined_jwo_20131225.owl

## Test Programs
### class_instance_extractor.RoleStatementsExtractor.scala
* Inputs
 * ontologies/wikipediaontology_instance_20101114ja.rdf
 * ontologies/wikipediaontology_class_20101114ja.rdf
* Ouput
 * inputs_and_outputs/tests/role_statements.txt

### class_instance_extractor.ClassInstanceExtractorFromRoleStatements.scala
* Input
 * inputs_and_outputs/tests/role_statements.txt
* Outputs
 * input_and_outputs/tests/role-property-class.txt
 * input_and_outputs/tests/role-property-class-cnt.csv
 * input_and_outputs/tests/class-instance_from_role_statements.txt

### class_instance_extractor.AnalyzeRoleStatements.scala
* Inputs
 * inputs_and_outputs/duplicated_class-instance_from_role.txt
 * inputs_and_outputs/class_instance_list_from_role.db
* Output
 * inputs_and_outputs/tests/added_class-instance_from_role.txt
