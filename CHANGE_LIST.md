Izvod 0.9 / API 00.07
- Target Java version is 17 now.
- Added new 'gate' property to the Flight entity.
- Added new 'status' property to the Flight entity.
- Data generator tries to create unique aircrafts now (try to avoid duplicate models).
- API
  - Added import-from-CSV-file operation for Aircraft.
  - Added async import-from-CSV-file operation for Aircraft.
  - Added export-to-CSV-file operation for Aircraft.
- GUI
  - Added import-from-CSV-file operation for Aircraft.
  - Altered search/new/list pages to support the 'gate' property in the Flight entity.
  - Altered search/new/list pages to support the 'status' property in the Flight entity.
  - Numerous minor improvements on the list/new Flight pages (values order in drop-downs, sorting order in the list etc).
--------------
Izvod 0.8 / API 00.062
- API
  - Improved DELETE and VERSION responses - a DTO is now used instead of plain String.
- GUI
  - Minor renaming.
--------------
Izvod 0.75 / API 00.06
- API
  - Improved validation and error handling.
- GUI
  - Improved logging.
--------------
Izvod 0.7 / API 00.05
- API
  - Added ``GET /paged`` operation, so it is possible now to specify page number, size etc.
- GUI
  - Added ``Flights list`` page for guest and agent users.
  - Altered some texts and button labels on the ``Verify Booking`` page.
  - Admin users can edit ``Aircraft`` entities now.
  - Admin users can edit ``Airport`` entities now.
--------------
Izvod 0.6 / API 00.04
- API
  - [HTTP JSON PATCH operations](https://www.baeldung.com/spring-rest-json-patch)
- GUI
  - Fixed some issues on the ``Verify Booking`` page.
  - Added tags for REST-controllers to prettify the Swagger page