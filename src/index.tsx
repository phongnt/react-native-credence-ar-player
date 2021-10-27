import { requireNativeComponent, ViewStyle } from 'react-native';
import Commands from './commands';

type CredenceArPlayerProps = {
  color: string;
  style: ViewStyle;
};

const CredenceArPlayerViewManager = requireNativeComponent<CredenceArPlayerProps>(
  'CredenceArPlayerView'
);

export default {CredenceArPlayerViewManager, Commands};
