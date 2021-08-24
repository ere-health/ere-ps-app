# Websocket Example messages

This folder contains some example messages that are currently supported by the ere-ps-app.


The following messages are currently supported:

 * SignAndUploadBundles - Signs and uploads the transferred bundles to the e-prescription service
   * Status messages are sent during the process:
     * HTMLBundles - This message contains the XSLT stylesheet of the KBV processed bundles that must be displayed to the user
     * BundlesValidationResult - If a bundle is invalid, this message will be sent with the appropriate validation error
     * Exception - Exceptions that can occur during processing
 messages and the Process is canceled
   * Response
     * ERezeptWithDocuments - Contains the signed bundles including AccessCode and PrescriptionID. Furthermore, the created PDF is encoded as base64 Document included
 * ValidateBundles - Validates the submitted bundles
   * Response
     * BundlesValidationResult - The corresponding validation messages
 * AbortTasks - Message to delete a task from the e-prescription service
   * Response
     * AbortTaskResponse - Status of whether the deletion was successful
 * RequestSettings - Query current settings including connector settings of the system
   * Response
     * Settings - list of current settings
 * SaveSettings - Save new settings
 * ActivateComfortSignature - enable comfort signature
   * Response
     * GetSignatureModeResponse contains details how long the session will be valid and how many signs are left
 * DeactiveComfortSignature - disable comfort signature
 * GetSignatureMode - Get the status of the current comfort signature session
   * Response
    * GetSignatureModeResponse contains details how long the session will be valid and how many signs are left