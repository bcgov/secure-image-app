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
// Created by Jason Leach on 2017-12-19.
//

import UIKit

class AlbumTableViewCell: UITableViewCell {

    @IBOutlet weak var coverImageView: UIImageView!
    @IBOutlet weak var albumTitleLabel: UILabel!
    @IBOutlet weak var titleOverlayView: UIView!
    
    internal static let fauxCellSpaceOffset: CGFloat = 24.0
    private static let cornerRadius: CGFloat = 10.0
    private var titleOverlayViewCornersRounded = false
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        commonInit()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Some white space on the cell is used for spacing so we can't round
        // the corners of the cell directly.
        if !titleOverlayViewCornersRounded {
            let mask = CAShapeLayer()
            let path = UIBezierPath(roundedRect: titleOverlayView.bounds, byRoundingCorners: [UIRectCorner.bottomLeft, UIRectCorner.bottomRight], cornerRadii: CGSize(width: AlbumTableViewCell.cornerRadius, height: AlbumTableViewCell.cornerRadius))
            mask.path = path.cgPath
            titleOverlayView.layer.mask = mask
        }
    }
    override func prepareForReuse() {
        
        coverImageView.image = UIImage(named: "album-placeholder")
    }
    
    private func commonInit() {
        
        contentView.backgroundColor = UIColor.white
        
        titleOverlayView.backgroundColor = Theme.albumOverlayBlue
        
        coverImageView.layer.cornerRadius = AlbumTableViewCell.cornerRadius
        coverImageView.layer.masksToBounds = true
    }
}
