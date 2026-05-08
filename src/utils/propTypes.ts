import type { PropType } from "vue";

export const propTypes = {
  number: {
    def: (defaultVal?: number) => ({
      type: Number as PropType<number>,
      default: defaultVal
    })
  },
  string: {
    def: (defaultVal?: string) => ({
      type: String as PropType<string>,
      default: defaultVal
    })
  },
  bool: {
    def: (defaultVal?: boolean) => ({
      type: Boolean as PropType<boolean>,
      default: defaultVal
    })
  }
};

export default propTypes;
