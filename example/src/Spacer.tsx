import React from 'react';
import { View, StyleSheet } from 'react-native';

const style = StyleSheet.create({
  spacer: {
    height: 10,
  },
});

export const Spacer = () => {
  return <View style={style.spacer} />;
};
