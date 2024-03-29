//
// SecureImage
//
// Copyright © 2017 Province of British Columbia
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Created by Jason Leach on 2017-12-14.
//

import UIKit
import RealmSwift
import MessageUI
import SingleSignOn

enum MailClient: String, CaseIterable {
    case Outlook
    case Apple
    case Gmail
    case Yahoo
}

class AlbumDetailsViewController: UIViewController {
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet var networkAvailabilityViewZeroHeightConstraint: NSLayoutConstraint!
    @IBOutlet var networkAvailabilityViewNormalHeightConstraint: NSLayoutConstraint!
    
    private static let captureImageSegueID = "CaptureImageSegue"
    private static let showImageSegueID = "ShowImageSegue"
    private static let showAllImagesSegueID = "ShowAllImagesSegue"
    private static let previewCellReuseID = "ImagePreviewCellID"
    private static let functionsCellReuseID = "FunctionsCellID"
    private static let annotationCellReuseID = "AnnotationCellID"
    private static let functionsCellRowHeight: CGFloat = 60.0
    private static let annotationCellRowHeight: CGFloat = 100.0
    private static let annotationCellsOffset: Int = 2
    private static let numberOfRows: Int = annotationCellsOffset + Constants.Album.Fields.count
    
    private var document: Document?
    private var previewCellHeight: CGFloat = 250.0
    
    private var progressOverlay: ProgressViewController = {
        let storyboard = UIStoryboard(name: "Progress", bundle: nil)
        let vc = storyboard.instantiateInitialViewController() as! ProgressViewController
        
        vc.modalPresentationStyle = .overCurrentContext
        return vc
    }()
    
    private let locationServices: LocationServices = {
        let ls = LocationServices()
        ls.start()
        
        return ls
    }()
    
    private let authServices: AuthServices = {
        return AuthServices(
            baseUrl: Constants.SSO.baseUrl,
            redirectUri: Constants.SSO.redirectUri,
            clientId: Constants.SSO.clientId,
            realm: Constants.SSO.realmName,
            idpHint: Constants.SSO.idpHint
        )
    }()
    
    internal var album: Album!
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        guard let _ = album else {
            fatalError("The album was not passed to \(AlbumDetailsViewController.self())")
        }
        
        commonInit()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        
        super.viewWillAppear(animated)
        
        tableView.reloadData()
    }
    
    override func willRotate(to toInterfaceOrientation: UIInterfaceOrientation, duration: TimeInterval) {
        
        tableView.reloadData()
    }
    
    // MARK: - Navigation
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        guard let segueID = segue.identifier else {
            return
        }
        
        switch segueID {
        case AlbumDetailsViewController.showImageSegueID:
            if let document = document, let dvc = segue.destination as? PhotoViewController {
                dvc.document = document
            }
        case AlbumDetailsViewController.showAllImagesSegueID:
            if let dvc = segue.destination as? PhotosViewController {
                dvc.album = album
            }
        case AlbumDetailsViewController.captureImageSegueID:
            if let dvc = segue.destination as? SecureCameraViewController {
                dvc.delegate = self
            }
        default:
            ()
        }
    }
    
    // MARK: -
    
    private func commonInit() {
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(AlbumDetailsViewController.keyboardWillShow),
            name: NSNotification.Name.UIKeyboardWillShow,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(AlbumDetailsViewController.keyboardWillHide),
            name: NSNotification.Name.UIKeyboardWillHide,
            object: nil
        )
        
        tableView.dataSource = self
        tableView.delegate = self
        tableView.allowsSelection = false
        
        if NetworkManager.shared.isReachableOnEthernetOrWiFi {
            networkAvailabilityViewZeroHeightConstraint.isActive = true
            networkAvailabilityViewNormalHeightConstraint.isActive = false
            view.setNeedsLayout()
        }
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(AlbumDetailsViewController.handleWiFiAvailabilityChanged(notification:)),
            name: Notification.Name.wifiAvailabilityChanged,
            object: nil
        )
    }
    
    private func configureCell(cell: UITableViewCell, at indexPath: IndexPath) {
        
        guard let identifier = cell.reuseIdentifier else {
            fatalError("Unable to determine cell reuse ID")
        }
        
        self.tableView.isEditing = false
        
        cell.contentView.backgroundColor = UIColor.white
        
        switch identifier {
        case AlbumDetailsViewController.previewCellReuseID:
            let cell = cell as! ImagePreviewTableViewCell
            cell.previewImages = album.documents
            cell.onViewImageTouched = { (document: Document) in
                self.document = document
                self.performSegue(withIdentifier: AlbumDetailsViewController.showImageSegueID, sender: nil)
            }
            cell.onAddImageTouched = {
                
                if !DataServices.canAddToAlbum(album: self.album) {
                    let title = "Album Limit"
                    let message = "You have reached the maximum photo count for this album."
                    
                    self.showAlert(with: title, message: message)
                    
                    return
                }
                
                self.performSegue(withIdentifier: AlbumDetailsViewController.captureImageSegueID, sender: nil)
            }
            cell.onPresentAlertRequested = { (alertView: UIAlertController) in
                self.present(alertView, animated: true, completion: nil)
            }
        case AlbumDetailsViewController.functionsCellReuseID:
            let cell = cell as! FuncitonTableViewCell
            
            if (album.documents.count == 0) {
                cell.disableUploadButton()
            } else {
                cell.enableUploadButton()
            }
            
            cell.onViewAllImagesTouched = {
                self.performSegue(withIdentifier: AlbumDetailsViewController.showAllImagesSegueID, sender: nil)
            }
            cell.onUploadAlbumTouched = {
                self.uploadAlbum()
            }
        default:
            let cell = cell as! AlbumPropertyTableViewCell
            let property = Constants.Album.Fields[indexPath.row - AlbumDetailsViewController.annotationCellsOffset]
            
            cell.attributeTitleLabel.text = property.name
            if let key = property.name.toCammelCase(), let value = album.value(forKey: key) as? String, !value.isEmpty {
                cell.attributeTextField.text = value
            } else {
                cell.attributeTextField.placeholder = property.placeHolderText
            }
            
            cell.onPropertyChanged = { (property: String, value: String) in
                self.updateAlbum(property: property, value: value)
            }
        }
    }
    
    private func uploadAlbum() {
        
        // Show appropriate message if no clients are available
        if availableEmailClients().isEmpty {
            let title = "Messaging"
            let message = "This devices is not able to send messages."
            showAlert(with: title, message: message)
            return
        }
        
        authenticateIfRequred()
    }
    
    private func uploadHandler() -> (() -> Void) {
        
        return {
            
            self.presentingViewController?.providesPresentationContextTransitionStyle = true
            self.presentingViewController?.definesPresentationContext = true
            self.present(self.progressOverlay, animated: true, completion: nil)
            let albumId = self.album.id;
            
            DispatchQueue.main.async {
                self.progressOverlay.updateProgress(to: 0.0)
            }
            
            guard let credentials = self.authServices.credentials else {
                return
            }
            
            BackendAPI.createAlbum(credentials: credentials) { (remoteAlbumId: String?) in
                
                guard let remoteAlbumId = remoteAlbumId, let realm = try? Realm(), let credentials = self.authServices.credentials else {
                    let message = "I wasn't able to create the remote album. Please try again later."
                    self.handleNetworkOperationFailed(message)
                    
                    return
                }
                
                do {
                    if let myAlbum = realm.objects(Album.self).filter("id == %@", albumId).first {
                        try realm.write {
                            myAlbum.remoteAlbumId = remoteAlbumId
                        }
                    }
                } catch {
                    print("WARN: Unable to create a remote album")
                }
                
                // We need to make sure all the images are uploaded before packaging the
                // album, to do this (quickly) the images are uploaded serially and the packaging
                // is done when the last image is done.
                
                let queue = OperationQueue()
                queue.maxConcurrentOperationCount = 1
                
                queue.addAsyncOperation { done in
                    var notes = [String:String]()
                    for field in Constants.Album.Fields {
                        if let realm = try? Realm(), let myAlbum = realm.objects(Album.self).filter("id == %@", albumId).first,
                           let key = field.name.toCammelCase(), let value = myAlbum.value(forKey: key) as? String {
                            notes[key] = value
                        }
                    }
                    
                    BackendAPI.addFieldNotes(credentials: credentials, parameters: notes, toRemoteAlbum: remoteAlbumId) { (error: Error?) in
                        
                        if let _ = error {
                            print("ERROR: Unable to add notes to album")
                        }
                        
                        done()
                    }
                }
                
                for item in self.album.documents.enumerated() {
                    let offset = item.offset
                    let doc = item.element
                    let copy = Data.init(base64Encoded: doc.imageData!.base64EncodedData())!
                    
                    queue.addAsyncOperation { done in
                        BackendAPI.add(credentials: credentials, image: copy, toRemoteAlbum: remoteAlbumId) { (remoteDocumentId: String?) in
                            
                            guard let remoteDocumentId = remoteDocumentId, let realm = try? Realm() else {
                                let message = "I wasn't able to add an image to the remote album. Please try later."
                                self.handleNetworkOperationFailed(message)
                                
                                return
                            }
                            
                            DispatchQueue.main.async {
                                print("progress = \(Float(offset) / Float(self.album.documents.count - 1))")
                                self.progressOverlay.updateProgress(to: (Float(offset) / Float(self.album.documents.count - 1)))
                            }
                            
                            do {
                                if let myDocument = realm.objects(Document.self).filter("id == %@", doc.id).first {
                                    try realm.write {
                                        myDocument.remoteDocumentId = remoteDocumentId
                                    }
                                }
                            } catch {
                                print("WARN: Unable to add image to remote album")
                            }
                            
                            if offset == self.album.documents.count - 1 {
                                self.progressOverlay.dismiss(animated: true, completion: {
                                    self.sendAlbumToMe()
                                })
                            }
                            
                            done()
                        }
                    }
                }
            }
        }
    }
    
    private func handleNetworkOperationFailed(_ message: String) {
        
        self.progressOverlay.dismiss(animated: true, completion: {
            let title = "Upload Problem"
            self.showAlert(with: title, message: message)
        })
    }
    
    private func sendAlbumToMe() {
        
        guard let remoteAlbumId = album.remoteAlbumId, let credentials = authServices.credentials else {
            return
        }
        
        BackendAPI.package(credentials: credentials, remoteAlbumId: remoteAlbumId) { (url: URL?) in
            
            guard let url = url else {
                let message = "I wasn't able to get the download URL for this album. Please try later."
                self.handleNetworkOperationFailed(message)
                
                return
            }
            
            self.sendEmailforUploaded(url: url)
        }
    }
    
    private func confirmNetworkAvailabilityBeforUpload(handler: @escaping (() -> Void)) {
        
        if  NetworkManager.shared.isReachableOnEthernetOrWiFi {
            handler()
            
            return
        }
        
        let title = "Network Availability"
        let message = "You are not connected to a WiFi network. Uploading now will use your mobile data."
        let ac = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let cancel = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        let upload = UIAlertAction(title: "Upload", style: .default) { (sender: UIAlertAction) in
            handler()
        }
        
        ac.addAction(cancel)
        ac.addAction(upload)
        
        present(ac, animated: true, completion: nil)
    }
    
    private func cellIdentifierForCell(at indexPath: IndexPath) -> String {
        
        var identifier = ""
        
        switch indexPath.row {
        case 0:
            identifier = AlbumDetailsViewController.previewCellReuseID
        case 1:
            identifier = AlbumDetailsViewController.functionsCellReuseID
        default:
            identifier = AlbumDetailsViewController.annotationCellReuseID
        }
        
        return identifier
    }
    
    private func authenticateIfRequred() {
        
        authServices.doWithAuthentication(presenting: self) { (credentials, error) in
            
            if let _ = credentials, error == nil {
                self.confirmNetworkAvailabilityBeforUpload(handler: self.uploadHandler())
            } else {
                let title = "Authentication"
                let message = "Authentication didn't work. Please try again."
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    self.showAlert(with: title, message: message)
                }
            }
            
        }
    }
    
    internal func updateAlbum(property: String, value: String) {
        
        do {
            let realm = try Realm()
            if let myAlbum = realm.objects(Album.self).filter("id == %@", album.id).first {
                try realm.write {
                    myAlbum.setValue(value, forKey: property)
                }
            }
        } catch {
            fatalError("Unable to update album properties")
        }
    }
    
    @objc dynamic private func handleWiFiAvailabilityChanged(notification: Notification) {
        
        networkAvailabilityViewZeroHeightConstraint.isActive = NetworkManager.shared.isReachableOnEthernetOrWiFi
        networkAvailabilityViewNormalHeightConstraint.isActive = !NetworkManager.shared.isReachableOnEthernetOrWiFi
        let animationDuration: Double = 0.33
        
        UIView.animate(withDuration: animationDuration) {
            
            self.view.layoutIfNeeded()
        }
    }
    
    @objc dynamic private func keyboardWillShow(notification: NSNotification) {
        
        guard let keyboardFrame: NSValue = notification.userInfo?[UIKeyboardFrameEndUserInfoKey] as? NSValue else {
            print("Could not extract the keyboard frame from the notification")
            return
        }
        
        // Get the cell the user is interacting with
        let keyboardRectangle = keyboardFrame.cgRectValue
        let keyboardHeight = keyboardRectangle.height
        let cell = tableView.visibleCells.map { (cell: UITableViewCell) -> UITableViewCell? in
            if let cell = cell as? AlbumPropertyTableViewCell, cell.attributeTextField.isFirstResponder {
                return cell
            }
            
            return nil
        }.filter { $0 != nil }.first
        
        // adjust the table so that the users current cell is just above
        // the top of the keyboard
        if let aCell = cell {
            // We want to move the table up the difference between the cells
            // current postion and the top of the keyboard.
            let delta = aCell!.center.y - keyboardHeight
            tableView.setContentOffset(CGPoint(x: 0, y: delta), animated: true)
        }
    }
    
    @objc dynamic private func keyboardWillHide(notification: NSNotification) {
        
        // reset the table to its initial state when the keyboard is gone
        tableView.setContentOffset(CGPoint(x: 0, y: 0), animated: true)
    }
}

// MARK: UITableViewDataSource
extension AlbumDetailsViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return AlbumDetailsViewController.numberOfRows
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let identifier = cellIdentifierForCell(at: indexPath)
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: identifier) else {
            fatalError("Unable to dequeue cell with identifier \(identifier)")
        }
        
        configureCell(cell: cell, at: indexPath)
        
        return cell
    }
}

// MARK: Email
extension AlbumDetailsViewController: MFMailComposeViewControllerDelegate {
    
    /// Shows UI for selecting an email client and redirects to the selected
    /// client app with a pre-populated message containing the given URL
    /// - Parameter url: to include in email body
    func sendEmailforUploaded(url: URL) {
        
        let availableClients = availableEmailClients()
        
        // Show client selection
        promptEmailClientSelection(options: availableClients) { selectedClient in
            // redirect to selected client
            self.sendEmailTo(
                mailClient: selectedClient,
                subject: self.formEmailSubject(),
                body: self.formEmailBody(with: url)
            )
        }
    }
    
    /// Presents a UI prompt for selecting an email client
    /// - Parameters:
    ///   - options: Email client options to show
    ///   - completion: selected email client
    func promptEmailClientSelection(options: [MailClient], completion: @escaping(MailClient)->Void) {
        
        let alert = UIAlertController(title: "Messaging", message: "Please choose an client", preferredStyle: .actionSheet)
        for option in options {
            alert.addAction(UIAlertAction(title: option.rawValue, style: .default , handler:{ (UIAlertAction) in
                return completion(option)
            }))
        }
        
        // TODO: check if ipad or iphone
        alert.popoverPresentationController?.sourceView = self.tableView
        
        self.present(alert, animated: true, completion: nil)
    }
    
    /// checks and returns available email clients available on device
    /// - Returns: Available Email clients on device
    func availableEmailClients() -> [MailClient] {
        var clients: [MailClient] = []
        
        if let yahooURI = URL(string: Constants.Email.Clients.yahooMailBaseURI), UIApplication.shared.canOpenURL(yahooURI) {
            clients.append(.Yahoo)
        }
        if let gmailURI = URL(string: Constants.Email.Clients.gmailBaseURI), UIApplication.shared.canOpenURL(gmailURI) {
            clients.append(.Gmail)
        }
        if let outlookURI = URL(string: Constants.Email.Clients.outlookBaseURI), UIApplication.shared.canOpenURL(outlookURI) {
            clients.append(.Outlook)
        }
        
        if MFMailComposeViewController.canSendMail() {
            clients.append(.Apple)
        }
        
        return clients
    }
    
    /// Generates email subject string based on current date
    /// - Returns: email subject
    func formEmailSubject() -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let createdAtAsString = dateFormatter.string(from: self.album.createdAt)
        return "Album from SecureImage App - Created \(createdAtAsString)"
    }
    
    /// generates email body with given url and current date
    /// - Parameter url: url to include in email body
    /// - Returns: email body string
    func formEmailBody(with url: URL) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        var expirationDateAsString = "Unknown"
        if let expirationDate = Calendar.current.date(byAdding: .day, value: Constants.Album.ExpirationInDays, to: Date()) {
            expirationDateAsString = dateFormatter.string(from: expirationDate)
        }
        
        // HTML body doesn't work when deep linking - would only work with native client
        /*
         return """
         Here is an album exported from SecureImage App.
         <br /><br />
         You can download the images from this album at the following URL:
         <br />
         <a href=\"\(url)\">Download Album</a>
         <br /><br />
         This link will expire \(Constants.Album.ExpirationInDays) days from today on \(expirationDateAsString).
         """
         */
        return """
                    Here is an album exported from SecureImage App.
                    \n
                    You can download the images from this album at the following URL:
                    \n
                    \(url.absoluteString)
                    \n
                    This link will expire \(Constants.Album.ExpirationInDays) days from today on \(expirationDateAsString).
                    """
    }
    
    /// Opens / redirects to the specified email client and populates the subject and body
    /// - Parameters:
    ///   - mailClient: email client to open
    ///   - subject: email subject
    ///   - body: email body
    func sendEmailTo(mailClient: MailClient, subject: String, body: String) {
        guard let subjectEncoded = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let bodyEncoded = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
                  return
              }
        let clientUrl: URL?
        switch mailClient {
        case .Outlook:
            clientUrl = URL(string: "\(Constants.Email.Clients.outlookBaseURI)?to=&subject=\(subjectEncoded)&body=\(bodyEncoded)")
        case .Apple:
            sendEmailWithAppleClient(subject: subject, body: body)
            return
        case .Gmail:
            clientUrl = URL(string: "\(Constants.Email.Clients.gmailBaseURI)?to=&subject=\(subjectEncoded)&body=\(bodyEncoded)")
        case .Yahoo:
            clientUrl = URL(string: "\(Constants.Email.Clients.yahooMailBaseURI)?to=&subject=\(subjectEncoded)&body=\(bodyEncoded)")
        }
        guard let clientLink = clientUrl, UIApplication.shared.canOpenURL(clientLink) else {
            let title = "\(mailClient.rawValue) is not available"
            let message = "Please try a different client"
            showAlert(with: title, message: message)
            return
        }
        
        UIApplication.shared.open(clientLink)
    }
    
    
    func sendEmailWithAppleClient(subject: String, body: String) {
        let composeVC = MFMailComposeViewController()
        composeVC.mailComposeDelegate = self
        composeVC.setSubject(subject)
        composeVC.setMessageBody(body, isHTML: true)
        
        self.present(composeVC, animated: true, completion: nil)
    }
    
    func mailComposeController(_ controller: MFMailComposeViewController,
                               didFinishWith result: MFMailComposeResult,
                               error: Error?) {
        
        switch result {
        case .failed:
            let title = "Messaging"
            let message = "The email message was not able to be sent. Please try again later"
            
            showAlert(with: title, message: message)
        default:
            ()
        }
        
        controller.dismiss(animated: true, completion: nil)
    }
}

// MARK: UITableViewDelegate
extension AlbumDetailsViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        let identifier = cellIdentifierForCell(at: indexPath)
        switch identifier
        {
        case AlbumDetailsViewController.previewCellReuseID:
            return previewCellHeight
        case AlbumDetailsViewController.functionsCellReuseID:
            return AlbumDetailsViewController.functionsCellRowHeight
        default:
            return AlbumDetailsViewController.annotationCellRowHeight
        }
    }
}

// MARK: SecureCameraImageCaptureDelegate
extension AlbumDetailsViewController: SecureCameraImageCaptureDelegate {
    
    func secureCamera(_ secureCameraViewController: SecureCameraViewController, captured image: Data) {
        
        guard let album = album else {
            fatalError("Unable unwrap album")
        }
        
        DataServices.add(image: image, to: album)
        
        if !DataServices.canAddToAlbum(album: album) {
            let title = "Album Limit"
            let message = "You have reached the maximum photo count for this album."
            secureCameraViewController.disable(title: title, message: message)
        }
    }
}

