PIXI Data Model
================

Small Animal Subject Model Details
----------------------------------
The table below shows demographic information managed by PIXI.
Rows marked in the column XNAT Core indicate demographic items that apply to both human subjects and small animals.
Rows marked with PIXI indicate items that are unique to the PIXI environment.

+-----------------------------------------+----------------+-----------+------+
|  Field                                  | Type           | Core XNAT | PIXI |
+=========================================+================+===========+======+
| Date of Birth                           | Date           |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Year of Birth                           | Integer        |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Age                                     | Integer        |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Gender                                  | Enumerated     |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Handedness                              | Enumerated     |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Socioeconomic Status                    | Integer        |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Employment Status                       | Enumerated     |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Education Level                         | Integer (0-30) |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Education Description                   | String         |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Race (multiple)                         | String         |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Ethnicity                               | String         |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Weight                                  | Float + units  |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Height                                  | Float + units  |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Gestational Age                         | Float          |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Post Menstrual Age                      | Float          |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Birth Weight                            | Float          |     X     |      |
+-----------------------------------------+----------------+-----------+------+
| Species                                 | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Strain                                  | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Source                                  | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Stock Number                            | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Genetic Modifications                   | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Genetic Modifications Summary           | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Date of Birth                           | Date           |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Sex                                     | Enumerated     |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Litter                                  | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+
| Strain Immune System Humanization Type  | String         |           |  X   |
+-----------------------------------------+----------------+-----------+------+

Experiment Data
---------------------

The XNAT Experiment Data type documents *a specific individual event in which primary data is obtained*.
XNAT maintains these just below the subject level, so a subject will have 0..N experiments.
Experiments can be CT, MR or PET imaging sessions, but are not limited to imaging data.
The baseline Experiment Data elements are listed in the table below.
The PIXI data types starting with *Caliper Measurements* include these baseline elements and
add the elements that are ascribed to the PIXI data type.
Some elements used to link an Experiment to other data such as the XNAT Project or Visit are omitted for brevity.

+-------------------------------------------+----------------+
|  Field                                    | Type           |
+===========================================+================+
| ID (primary key)                          | String         |
+-------------------------------------------+----------------+
| Date                                      | Date           |
+-------------------------------------------+----------------+
| Time                                      | Time           |
+-------------------------------------------+----------------+
| Duration                                  | Duration       |
+-------------------------------------------+----------------+
| Delay                                     | Integer        |
+-------------------------------------------+----------------+
| Note                                      | String         |
+-------------------------------------------+----------------+
| Investigator (Name, Institution, Dept...) | Composite      |
+-------------------------------------------+----------------+
| Validation (Date, Method, Status, ...)    | Composite      |
+-------------------------------------------+----------------+
| Acquisition Site                          | String         |
+-------------------------------------------+----------------+
| Version                                   | Integer        |
+-------------------------------------------+----------------+
| Original                                  | String         |
+-------------------------------------------+----------------+
| Protocol                                  | String         |
+-------------------------------------------+----------------+
| Label                                     | String         |
+-------------------------------------------+----------------+

Caliper Measurements
---------------------

*An event in which a subject's tumor volume is measured manually using a caliper measurement tool.*

+-----------------------------------------+----------------+
|  Field                                  | Type           |
+=========================================+================+
| Length                                  | Date           |
+-----------------------------------------+----------------+
| Width                                   | Date           |
+-----------------------------------------+----------------+
| Unit                                    | Date           |
+-----------------------------------------+----------------+
| Technician                              | Date           |
+-----------------------------------------+----------------+
| Weight                                  | Date           |
+-----------------------------------------+----------------+
| Weight Unit                             | Date           |
+-----------------------------------------+----------------+


Drug Therapy
---------------------

*An event in which a subject is administered a drug or medication.*

+-----------------------------------------+----------------+
|  Field                                  | Type           |
+=========================================+================+
| Drug                                    | String         |
+-----------------------------------------+----------------+
| Dose                                    | Decimal        |
+-----------------------------------------+----------------+
| Dose Unit (e.g., mg)                    | String         |
+-----------------------------------------+----------------+
| Lot Number                              | String         |
+-----------------------------------------+----------------+
| Route (e.g.., oral)                     | String         |
+-----------------------------------------+----------------+
| Site                                    | String         |
+-----------------------------------------+----------------+
| Technician                              | String         |
+-----------------------------------------+----------------+
| (Subject) Weight                        | Decimal        |
+-----------------------------------------+----------------+
| Weight Unit                             | Enumerated     |
+-----------------------------------------+----------------+


Weight
---------------------

*An event in which a subject's weight is obtained outside the context of an imaging experiment.*

+-----------------------------------------+----------------+
|  Field                                  | Type           |
+=========================================+================+
| Weight                                  | Decimal        |
+-----------------------------------------+----------------+
| Unit                                    | Enumerated     |
+-----------------------------------------+----------------+
| Technician                              | String         |
+-----------------------------------------+----------------+

Cell Line
---------------------

*An event in which a cell line is injected into a small animal subject to create an animal model that can be used for preclinical research.*

+-------------------------------------------+----------------+
|  Field                                    | Type           |
+===========================================+================+
| Source ID (Owned by the source, not XNAT) | String         |
+-------------------------------------------+----------------+
| Injection Site                            | String         |
+-------------------------------------------+----------------+
| Injection Type                            | String         |
+-------------------------------------------+----------------+
| Number of Cells Injected                  | Positive Int   |
+-------------------------------------------+----------------+


Patient Derived Xenograft
---------------------

*An event in which human tumor tissue is engrafted into a small animal subject to create an animal model that can be used for preclinical research.*

+-------------------------------------------+----------------+
|  Field                                    | Type           |
+===========================================+================+
| Source ID (Owned by the source, not XNAT) | String         |
+-------------------------------------------+----------------+
| Injection Site                            | String         |
+-------------------------------------------+----------------+
| Injection Type                            | String         |
+-------------------------------------------+----------------+
| Number of Cells Injected                  | Positive Int   |
+-------------------------------------------+----------------+
| Passage                                   | String         |
+-------------------------------------------+----------------+
| Passage Method                            | String         |
+-------------------------------------------+----------------+

Animal Husbandry
---------------------

*Record animal feeding and housing information over an interval during which conditions are relatively homogeneous.*

+-------------------------------------------+----------------+
|  Field                                    | Type           |
+===========================================+================+
| Animal Feed                               | String         |
+-------------------------------------------+----------------+
| Feed Source                               | String         |
+-------------------------------------------+----------------+
| Feed Manufacturer                         | String         |
+-------------------------------------------+----------------+
| Feed Product Name                         | String         |
+-------------------------------------------+----------------+
| Feed Product Code                         | String         |
+-------------------------------------------+----------------+
| Feeding Method                            | String         |
+-------------------------------------------+----------------+
| Water Type                                | String         |
+-------------------------------------------+----------------+
| Water Deliver                             | String         |
+-------------------------------------------+----------------+
| Number of Animals In Same Housing Unit    | Integer        |
+-------------------------------------------+----------------+
| Sex of Animals In Same Housing Unit       | Decimal        |
+-------------------------------------------+----------------+
| Housing Humidity                          | Decimal        |
+-------------------------------------------+----------------+
