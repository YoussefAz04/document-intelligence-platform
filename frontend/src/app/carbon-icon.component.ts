import { Component, Input } from '@angular/core';
import Add16 from '@carbon/icons/es/add/16.js';
import AI16 from '@carbon/icons/es/AI/16.js';
import AI20 from '@carbon/icons/es/AI/20.js';
import AI24 from '@carbon/icons/es/AI/24.js';
import ArrowRight16 from '@carbon/icons/es/arrow--right/16.js';
import Chat20 from '@carbon/icons/es/chat/20.js';
import CheckmarkFilled20 from '@carbon/icons/es/checkmark--filled/20.js';
import Close16 from '@carbon/icons/es/close/16.js';
import Document20 from '@carbon/icons/es/document/20.js';
import Document24 from '@carbon/icons/es/document/24.js';
import DocumentMultiple20 from '@carbon/icons/es/document--multiple-01/20.js';
import DataBase20 from '@carbon/icons/es/data--base/20.js';
import Menu20 from '@carbon/icons/es/menu/20.js';
import Renew16 from '@carbon/icons/es/renew/16.js';
import Send20 from '@carbon/icons/es/send/20.js';
import Search16 from '@carbon/icons/es/search/16.js';
import Settings20 from '@carbon/icons/es/settings/20.js';
import ThumbsDown16 from '@carbon/icons/es/thumbs-down/16.js';
import ThumbsDownFilled16 from '@carbon/icons/es/thumbs-down--filled/16.js';
import ThumbsUp16 from '@carbon/icons/es/thumbs-up/16.js';
import ThumbsUp20 from '@carbon/icons/es/thumbs-up/20.js';
import ThumbsUpFilled16 from '@carbon/icons/es/thumbs-up--filled/16.js';
import Upload16 from '@carbon/icons/es/upload/16.js';
import Upload24 from '@carbon/icons/es/upload/24.js';
import User20 from '@carbon/icons/es/user/20.js';
import WarningAlt20 from '@carbon/icons/es/warning--alt/20.js';

type IconName =
  | 'add'
  | 'ai'
  | 'arrow--right'
  | 'chat'
  | 'checkmark--filled'
  | 'close'
  | 'document'
  | 'document--multiple'
  | 'data--base'
  | 'menu'
  | 'renew'
  | 'send'
  | 'search'
  | 'settings'
  | 'thumbs-down'
  | 'thumbs-down--filled'
  | 'thumbs-up'
  | 'thumbs-up--filled'
  | 'upload'
  | 'user'
  | 'warning--alt';

interface IconPath {
  attrs: Record<string, string>;
}

interface IconDescriptor {
  attrs: { viewBox: string };
  content: IconPath[];
}

const ICONS: Record<string, IconDescriptor> = {
  'add-16': Add16 as IconDescriptor,
  'ai-16': AI16 as IconDescriptor,
  'ai-20': AI20 as IconDescriptor,
  'ai-24': AI24 as IconDescriptor,
  'arrow--right-16': ArrowRight16 as IconDescriptor,
  'chat-20': Chat20 as IconDescriptor,
  'checkmark--filled-20': CheckmarkFilled20 as IconDescriptor,
  'close-16': Close16 as IconDescriptor,
  'document-20': Document20 as IconDescriptor,
  'document-24': Document24 as IconDescriptor,
  'document--multiple-20': DocumentMultiple20 as IconDescriptor,
  'data--base-20': DataBase20 as IconDescriptor,
  'menu-20': Menu20 as IconDescriptor,
  'renew-16': Renew16 as IconDescriptor,
  'send-20': Send20 as IconDescriptor,
  'search-16': Search16 as IconDescriptor,
  'settings-20': Settings20 as IconDescriptor,
  'thumbs-down-16': ThumbsDown16 as IconDescriptor,
  'thumbs-down--filled-16': ThumbsDownFilled16 as IconDescriptor,
  'thumbs-up-16': ThumbsUp16 as IconDescriptor,
  'thumbs-up-20': ThumbsUp20 as IconDescriptor,
  'thumbs-up--filled-16': ThumbsUpFilled16 as IconDescriptor,
  'upload-16': Upload16 as IconDescriptor,
  'upload-24': Upload24 as IconDescriptor,
  'user-20': User20 as IconDescriptor,
  'warning--alt-20': WarningAlt20 as IconDescriptor,
};

@Component({
  selector: 'app-carbon-icon',
  template: `
    <svg
      xmlns="http://www.w3.org/2000/svg"
      [attr.viewBox]="descriptor.attrs.viewBox"
      [attr.width]="size"
      [attr.height]="size"
      fill="currentColor"
      aria-hidden="true"
      focusable="false"
    >
      @for (path of descriptor.content; track $index) {
        <path
          [attr.d]="path.attrs['d']"
          [attr.fill]="path.attrs['fill']"
          [attr.fill-rule]="path.attrs['fill-rule']"
          [attr.opacity]="path.attrs['opacity']"
        />
      }
    </svg>
  `,
  styles: `
    :host {
      display: inline-flex;
      flex: 0 0 auto;
      line-height: 0;
    }
  `,
})
export class CarbonIconComponent {
  @Input({ required: true }) name!: IconName;
  @Input() size: 16 | 20 | 24 = 20;

  get descriptor(): IconDescriptor {
    return ICONS[`${this.name}-${this.size}`];
  }
}
